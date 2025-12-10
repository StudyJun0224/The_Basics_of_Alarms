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
import java.io.IOException

class InferenceManager(private val context: Context) {

    private var module: Module? = null
    private val MODEL_NAME = "model_plan_b_prime.ptl"

    init {
        loadModel()
    }

    private fun loadModel() {
        try {
            // [수정] assets 파일을 내부 저장소로 복사 후 로드
            val modelPath = assetFilePath(context, MODEL_NAME)
            module = LiteModuleLoader.load(modelPath)
            Log.d(TAG, "PyTorch Model loaded successfully from $modelPath")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load PyTorch model", e)
        }
    }

    // [추가] Assets -> 내부 저장소 복사 헬퍼 함수
    @Throws(IOException::class)
    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }

        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (inputStream.read(buffer).also { read = it } != -1) {
                    outputStream.write(buffer, 0, read)
                }
                outputStream.flush()
            }
        }
        return file.absolutePath
    }

    fun predict(accBuffer: List<Triple<Float, Float, Float>>, hrFeatures: FloatArray): SleepStage {
        val model = module ?: return SleepStage.UNKNOWN

        try {
            // 1. Prepare ACC Tensor [1, 750, 3] -> Flattened [1, 2250]
            val flatAcc = FloatArray(accBuffer.size * 3)
            for (i in accBuffer.indices) {
                flatAcc[i * 3] = accBuffer[i].first
                flatAcc[i * 3 + 1] = accBuffer[i].second
                flatAcc[i * 3 + 2] = accBuffer[i].third
            }
            val accTensor = Tensor.fromBlob(flatAcc, longArrayOf(1, 750, 3))

            // 2. Prepare HR Tensor [1, 5]
            val hrTensor = Tensor.fromBlob(hrFeatures, longArrayOf(1, 5))

            // 3. Inference
            val outputTensor = model.forward(IValue.from(accTensor), IValue.from(hrTensor)).toTensor()
            val scores = outputTensor.dataAsFloatArray

            // 4. ArgMax (Get highest score index)
            val maxScoreIndex = scores.indices.maxByOrNull { scores[it] } ?: 0

            // 5. Map to SleepStage
            return when (maxScoreIndex) {
                0 -> SleepStage.WAKE
                1 -> SleepStage.LIGHT
                2 -> SleepStage.DEEP
                3 -> SleepStage.REM
                else -> SleepStage.UNKNOWN
            }

        } catch (e: Exception) {
            Log.e(TAG, "Inference Error", e)
            return SleepStage.UNKNOWN
        }
    }

    companion object {
        private const val TAG = "InferenceManager"
    }
}
