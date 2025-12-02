package com.example.sleeptandard_mvp_demo.service

import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import com.example.sleeptandard_mvp_demo.backend.model.SensorType
import com.example.sleeptandard_mvp_demo.backend.processing.FeatureExtractor
import com.example.sleeptandard_mvp_demo.backend.repository.DataRepository
import com.example.sleeptandard_mvp_demo.backend.repository.UserStatsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.ArrayDeque

class SmartAlarmService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var dataRepository: DataRepository
    private lateinit var userStatsManager: UserStatsManager
    private val featureExtractor = FeatureExtractor()
    private val serviceScope = CoroutineScope(Dispatchers.Default)

    private val HR_WINDOW_SIZE = 30
    private val hrWindow = ArrayDeque<Float>(HR_WINDOW_SIZE)
    private var lastFeatureExtractionTime = 0L
    private val FEATURE_INTERVAL_MS = 5000L 

    private val ACC_SAMPLE_RATE_US = 40000 
    private val HR_SAMPLE_RATE_US = 1000000 

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        dataRepository = DataRepository(this)
        userStatsManager = UserStatsManager(this)

        val hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        hrSensor?.let { sensorManager.registerListener(this, it, HR_SAMPLE_RATE_US) }
        accSensor?.let { sensorManager.registerListener(this, it, ACC_SAMPLE_RATE_US) }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val timestamp = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                // 가속도 값도 유효성 검사 권장
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                // [안정성] 비정상 값 필터링
                if (x.isFinite() && y.isFinite() && z.isFinite()) {
                    dataRepository.enqueueSensorData(timestamp, SensorType.ACC, x, y, z)
                }
            }
            Sensor.TYPE_HEART_RATE -> {
                val hrValue = event.values[0]
                
                // [핵심 수정] NaN/Infinite 값이 들어오면 즉시 리턴하여 로그 오염 및 로직 오류 방지
                if (!hrValue.isFinite() || hrValue <= 0) return

                // 1. 로깅 (이제 안전한 값만 기록됨)
                dataRepository.enqueueSensorData(timestamp, SensorType.HR, hrValue, 0f, 0f)
                
                // 2. 통계 업데이트
                userStatsManager.update(hrValue)

                // 3. 윈도우 로직
                handleHeartRateLogic(hrValue, timestamp)
            }
        }
    }

    private fun handleHeartRateLogic(hrValue: Float, timestamp: Long) {
        val shouldRunInference: Boolean
        val hrSnapshot: List<Float>

        synchronized(hrWindow) {
            if (hrWindow.size >= HR_WINDOW_SIZE) {
                hrWindow.removeFirst()
            }
            hrWindow.addLast(hrValue)

            shouldRunInference = (hrWindow.size == HR_WINDOW_SIZE) &&
                    (timestamp - lastFeatureExtractionTime >= FEATURE_INTERVAL_MS)
            
            hrSnapshot = if (shouldRunInference) hrWindow.toList() else emptyList()
        }

        if (shouldRunInference) {
            lastFeatureExtractionTime = timestamp
            runInferencePipeline(timestamp, hrSnapshot)
        }
    }

    private fun runInferencePipeline(timestamp: Long, hrBuffer: List<Float>) {
        serviceScope.launch {
            val userMean = userStatsManager.getUserMean()
            val userStd = userStatsManager.getUserStd()

            val features = featureExtractor.getFeatures(hrBuffer, userMean, userStd)
            val featureString = features.joinToString(",")

            // TFLite 예측...
            
            dataRepository.enqueueInferenceLog(timestamp, "Stage_Unknown,0.0,$featureString")
        }
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        dataRepository.stopLogging()
        serviceScope.cancel()
        super.onDestroy()
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null
}