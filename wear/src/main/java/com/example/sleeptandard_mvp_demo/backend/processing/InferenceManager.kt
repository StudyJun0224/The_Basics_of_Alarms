package com.example.sleeptandard_mvp_demo.backend.processing

import android.content.Context
import android.util.Log
import com.example.sleeptandard_mvp_demo.backend.model.SleepStage
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Module
import org.pytorch.Tensor
import java.io.File
import java.io.FileOutputStream

/**
 * PyTorch Mobile 기반 실시간 수면 단계 추론 엔진
 * 
 * Model Input:
 * - ACC Tensor: [1, 750, 3] (30초 * 25Hz)
 * - HR Features: [1, 5] (normalized mean, std, max, min, approx_rmssd)
 * 
 * Model Output:
 * - Class Logits: [1, 4] (WAKE, LIGHT, DEEP, REM)
 */
class InferenceManager(context: Context) {

    private var model: Module? = null
    private var isInitialized = false

    companion object {
        private const val TAG = "InferenceManager"
        private const val MODEL_FILE_NAME = "model_plan_b_prime.ptl"
        
        // Model expects 4 classes: WAKE, LIGHT, DEEP, REM
        private const val NUM_CLASSES = 4
        
        // Input dimensions
        private const val ACC_WINDOW_SIZE = 750  // 30s * 25Hz
        private const val ACC_FEATURES = 3       // X, Y, Z
        private const val HR_FEATURES_SIZE = 5   // mean, std, max, min, rmssd
    }

    init {
        try {
            Log.d(TAG, "Initializing PyTorch Mobile model...")
            
            // Load model from assets to internal storage (PyTorch requirement)
            val modelFile = assetFilePath(context, MODEL_FILE_NAME)
            
            // Load model using LiteModuleLoader
            model = LiteModuleLoader.load(modelFile)
            isInitialized = true
            
            Log.i(TAG, "Model loaded successfully from $modelFile")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize PyTorch model", e)
            isInitialized = false
        }
    }

    /**
     * 모델 파일을 Assets에서 Internal Storage로 복사
     * (PyTorch는 파일 경로가 필요함)
     */
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        
        // 이미 파일이 존재하면 재사용
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        // Assets에서 복사
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4096)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }
        
        return file.absolutePath
    }

    /**
     * 실시간 수면 단계 예측
     * 
     * @param accBuffer ACC 데이터 (750개, 3축)
     * @param hrFeatures HR 특성 벡터 (5개)
     * @return 예측된 SleepStage와 신뢰도
     */
    fun predict(
        accBuffer: List<Triple<Float, Float, Float>>,
        hrFeatures: FloatArray
    ): Pair<SleepStage, Float> {
        
        // 모델 초기화 실패 시 UNKNOWN 반환
        if (!isInitialized || model == null) {
            Log.w(TAG, "Model not initialized, returning UNKNOWN")
            return Pair(SleepStage.UNKNOWN, 0.0f)
        }

        try {
            // Input Validation
            if (accBuffer.size < ACC_WINDOW_SIZE) {
                Log.w(TAG, "Insufficient ACC data: ${accBuffer.size} < $ACC_WINDOW_SIZE")
                return Pair(SleepStage.UNKNOWN, 0.0f)
            }
            
            if (hrFeatures.size != HR_FEATURES_SIZE) {
                Log.w(TAG, "Invalid HR features size: ${hrFeatures.size} != $HR_FEATURES_SIZE")
                return Pair(SleepStage.UNKNOWN, 0.0f)
            }

            // === Input 1: ACC Tensor [1, 750, 3] ===
            val accTensor = prepareAccTensor(accBuffer)
            
            // === Input 2: HR Features [1, 5] ===
            val hrTensor = prepareHrTensor(hrFeatures)
            
            // === Forward Pass ===
            val outputTensor = model!!.forward(
                IValue.from(accTensor),
                IValue.from(hrTensor)
            ).toTensor()
            
            // === Output Processing ===
            val scores = outputTensor.dataAsFloatArray
            
            if (scores.size != NUM_CLASSES) {
                Log.e(TAG, "Unexpected output size: ${scores.size} != $NUM_CLASSES")
                return Pair(SleepStage.UNKNOWN, 0.0f)
            }
            
            // Find ArgMax (가장 높은 점수의 클래스)
            val predictedClassIndex = scores.indices.maxByOrNull { scores[it] } ?: 0
            val confidence = scores[predictedClassIndex]
            
            // Map index to SleepStage
            val predictedStage = when (predictedClassIndex) {
                0 -> SleepStage.WAKE
                1 -> SleepStage.LIGHT
                2 -> SleepStage.DEEP
                3 -> SleepStage.REM
                else -> SleepStage.UNKNOWN
            }
            
            Log.d(TAG, "Model Prediction: $predictedStage (Confidence: $confidence)")
            
            return Pair(predictedStage, confidence)
            
        } catch (e: Exception) {
            Log.e(TAG, "Inference failed", e)
            return Pair(SleepStage.UNKNOWN, 0.0f)
        }
    }

    /**
     * ACC 데이터를 Tensor [1, 750, 3]로 변환
     */
    private fun prepareAccTensor(accBuffer: List<Triple<Float, Float, Float>>): Tensor {
        // Take exactly 750 samples (latest window)
        val samples = accBuffer.takeLast(ACC_WINDOW_SIZE)
        
        // Flatten to [1, 750, 3] shape
        val flatArray = FloatArray(ACC_WINDOW_SIZE * ACC_FEATURES)
        
        for (i in samples.indices) {
            val (x, y, z) = samples[i]
            flatArray[i * 3 + 0] = x
            flatArray[i * 3 + 1] = y
            flatArray[i * 3 + 2] = z
        }
        
        // Create Tensor with shape [1, 750, 3]
        return Tensor.fromBlob(
            flatArray,
            longArrayOf(1, ACC_WINDOW_SIZE.toLong(), ACC_FEATURES.toLong())
        )
    }

    /**
     * HR Features를 Tensor [1, 5]로 변환
     */
    private fun prepareHrTensor(hrFeatures: FloatArray): Tensor {
        // Create Tensor with shape [1, 5]
        return Tensor.fromBlob(
            hrFeatures,
            longArrayOf(1, HR_FEATURES_SIZE.toLong())
        )
    }

    /**
     * 리소스 정리
     */
    fun release() {
        try {
            model?.destroy()
            model = null
            isInitialized = false
            Log.d(TAG, "Model resources released")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release model", e)
        }
    }
}

