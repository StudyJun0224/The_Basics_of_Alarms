package com.example.sleeptandard_mvp_demo.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sleeptandard_mvp_demo.R
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

    // WakeLock for preventing CPU sleep during tracking
    private lateinit var wakeLock: PowerManager.WakeLock
    
    // Synchronization lock for window access
    private val windowLock = Any()

    // Window sizes (30s intervals for power optimization)
    private val HR_WINDOW_SIZE = 30  // 30 seconds * 1Hz
    private val ACC_WINDOW_SIZE = 750  // 30 seconds * 25Hz
    
    private val hrWindow = ArrayDeque<Float>(HR_WINDOW_SIZE)
    private val accWindow = ArrayDeque<Triple<Float, Float, Float>>(ACC_WINDOW_SIZE)
    private var lastFeatureExtractionTime = 0L

    // Sampling rates and intervals (Power Budget Optimization)
    private val ACC_SAMPLE_RATE_US = 40000  // 25Hz
    private val HR_SAMPLE_RATE_US = 1000000  // 1Hz
    private val BATCH_LATENCY_US = 30_000_000  // 30 seconds batch latency
    private val FEATURE_INTERVAL_MS = 30000L  // 30 seconds inference interval 

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "SmartAlarmService onCreate()")
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service Started with Batch=30s, Interval=30s")
        
        try {
            // 1. Initialize WakeLock
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "SmartAlarm:TrackingWakeLock"
            )
            
            if (!wakeLock.isHeld) {
                wakeLock.acquire(8 * 60 * 60 * 1000L) // Max 8 hours
                Log.d(TAG, "WakeLock acquired")
            }
            
            // 2. Initialize backend components
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            dataRepository = DataRepository(this)
            userStatsManager = UserStatsManager(this)
            
            // 3. Register sensors with hardware batching
            val hrSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
            val accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            
            if (hrSensor == null || accSensor == null) {
                throw IllegalStateException("Required sensors not available")
            }
            
            // Register HR sensor (1Hz, no batching needed)
            sensorManager.registerListener(this, hrSensor, HR_SAMPLE_RATE_US)
            
            // Register ACC sensor with 30s batch latency (Power Optimization)
            sensorManager.registerListener(
                this, 
                accSensor, 
                ACC_SAMPLE_RATE_US,
                BATCH_LATENCY_US
            )
            
            Log.d(TAG, "Sensors registered with 30s batch latency")
            
            // 4. Create notification and promote to foreground
            createNotificationChannel()
            val notification = buildNotification()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                startForeground(
                    NOTIFICATION_ID,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            
            Log.i(TAG, "Foreground service started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Service initialization failed", e)
            
            // Release WakeLock if acquired
            try {
                if (::wakeLock.isInitialized && wakeLock.isHeld) {
                    wakeLock.release()
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Failed to release WakeLock during cleanup", ex)
            }
            
            // Stop service immediately
            stopSelf()
            return START_NOT_STICKY
        }
        
        return START_NOT_STICKY
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Sleep Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors sleep stages using sensors"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun buildNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sleep Tracking Active")
            .setContentText("Monitoring sensors...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setShowWhen(false)
            .build()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event ?: return
        val timestamp = System.currentTimeMillis()

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]
                
                if (!x.isFinite() || !y.isFinite() || !z.isFinite()) return
                
                // Log raw data
                dataRepository.enqueueSensorData(timestamp, SensorType.ACC, x, y, z)
                
                // Update ACC window with thread safety
                synchronized(windowLock) {
                    if (accWindow.size >= ACC_WINDOW_SIZE) {
                        accWindow.removeFirst()
                    }
                    accWindow.addLast(Triple(x, y, z))
                }
            }
            
            Sensor.TYPE_HEART_RATE -> {
                val hrValue = event.values[0]
                
                if (!hrValue.isFinite() || hrValue <= 0) return

                // Log raw data
                dataRepository.enqueueSensorData(timestamp, SensorType.HR, hrValue, 0f, 0f)
                
                // Update statistics
                userStatsManager.update(hrValue)

                // Update HR window and check inference trigger
                handleHeartRateLogic(hrValue, timestamp)
            }
        }
    }

    private fun handleHeartRateLogic(hrValue: Float, timestamp: Long) {
        val shouldRunInference: Boolean
        val hrSnapshot: List<Float>
        val accSnapshot: List<Triple<Float, Float, Float>>

        synchronized(windowLock) {
            // Update HR window
            if (hrWindow.size >= HR_WINDOW_SIZE) {
                hrWindow.removeFirst()
            }
            hrWindow.addLast(hrValue)

            // Check if inference should run (60s interval)
            shouldRunInference = (hrWindow.size == HR_WINDOW_SIZE) &&
                    (accWindow.size >= ACC_WINDOW_SIZE) &&
                    (timestamp - lastFeatureExtractionTime >= FEATURE_INTERVAL_MS)
            
            if (shouldRunInference) {
                hrSnapshot = hrWindow.toList()
                accSnapshot = accWindow.toList()
            } else {
                hrSnapshot = emptyList()
                accSnapshot = emptyList()
            }
        }

        if (shouldRunInference) {
            lastFeatureExtractionTime = timestamp
            runInferencePipeline(timestamp, hrSnapshot, accSnapshot)
        }
    }

    private fun runInferencePipeline(
        timestamp: Long, 
        hrBuffer: List<Float>,
        accBuffer: List<Triple<Float, Float, Float>>
    ) {
        serviceScope.launch {
            val userMean = userStatsManager.getUserMean()
            val userStd = userStatsManager.getUserStd()

            // Extract HR features
            val hrFeatures = featureExtractor.getFeatures(hrBuffer, userMean, userStd)
            val featureString = hrFeatures.joinToString(",")

            // TODO: Extract ACC features (magnitude, variance, etc.) from accBuffer
            // Currently using only HR features for MVP
            
            // TODO: TFLite model inference with combined features
            
            dataRepository.enqueueInferenceLog(
                timestamp, 
                "Stage_Unknown,0.0,$featureString,accSamples=${accBuffer.size}"
            )
            
            Log.d(TAG, "Inference completed: HR=${hrBuffer.size}, ACC=${accBuffer.size}")
        }
    }

    override fun onDestroy() {
        // 1. Unregister sensors to stop data flow
        sensorManager.unregisterListener(this)
        
        // 2. Stop logging and flush remaining data
        dataRepository.stopLogging()
        
        // 3. Cancel coroutine scope
        serviceScope.cancel()
        
        // 4. Release WakeLock
        try {
            if (wakeLock.isHeld) {
                wakeLock.release()
                Log.d(TAG, "WakeLock released")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release WakeLock", e)
        }
        
        super.onDestroy()
        Log.d(TAG, "SmartAlarmService destroyed")
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    override fun onBind(intent: Intent?): IBinder? = null
    
    companion object {
        private const val TAG = "SmartAlarmService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "sleep_tracking_channel"
    }
}