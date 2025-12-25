package com.example.sleeptandard_mvp_demo.backend.service

// [필수 Import 추가됨]
import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.sleeptandard_mvp_demo.PermissionActivity
import com.example.sleeptandard_mvp_demo.service.SmartAlarmService
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.ByteBuffer

/**
 * WatchListenerService - 폰으로부터 명령을 수신하는 서비스
 */
class WatchListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Message received: ${messageEvent.path}")

        when (messageEvent.path) {
            PATH_START_TRACKING -> {
                handleStartTracking(messageEvent.data)
            }
            PATH_STOP_TRACKING -> {
                handleStopTracking()
            }
            else -> {
                Log.w(TAG, "Unknown message path: ${messageEvent.path}")
            }
        }
    }

    private fun handleStartTracking(data: ByteArray) {
        try {
            if (data.size < 8) return
            val targetAlarmTime = ByteBuffer.wrap(data).long
            Log.i(TAG, "START_TRACKING received. Target: $targetAlarmTime")

            // 1. 필수 권한 목록 확인 (런타임 권한만)
            // 주의: REQUEST_IGNORE_BATTERY_OPTIMIZATIONS는 포함하지 말것!
            // Foreground Service 환경에서는 BODY_SENSORS만으로 충분
            val permissions = arrayOf(
                Manifest.permission.BODY_SENSORS,
                Manifest.permission.ACTIVITY_RECOGNITION,
                Manifest.permission.POST_NOTIFICATIONS
            )

            // 2. 권한이 모두 있는지 체크
            val allPermissionsGranted = permissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }

            // 3. 배터리 최적화 예외 상태 확인
            val isBatteryOptimizationIgnored = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
                powerManager.isIgnoringBatteryOptimizations(packageName)
            } else {
                true // Android M 미만은 배터리 최적화 기능 없음
            }

            // 4. 권한과 배터리 최적화 모두 확인
            if (allPermissionsGranted && isBatteryOptimizationIgnored) {
                // 4-A. 모든 조건 충족 -> 바로 서비스 시작
                Log.i(TAG, "All permissions and battery optimization granted. Starting service.")
                val intent = Intent(this, SmartAlarmService::class.java).apply {
                    putExtra(SmartAlarmService.EXTRA_TARGET_TIME, targetAlarmTime)
                    action = SmartAlarmService.ACTION_START_TRACKING
                }
                startForegroundService(intent)
            } else {
                // 4-B. 권한 또는 배터리 최적화 미충족 -> PermissionActivity 실행
                if (!allPermissionsGranted) {
                    Log.w(TAG, "Runtime permissions missing. Launching PermissionActivity.")
                }
                if (!isBatteryOptimizationIgnored) {
                    Log.w(TAG, "Battery optimization not ignored. Launching PermissionActivity.")
                }

                val intent = Intent(this, PermissionActivity::class.java).apply {
                    putExtra(SmartAlarmService.EXTRA_TARGET_TIME, targetAlarmTime)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK) // 서비스에서 액티비티 켤 때 필수
                }
                startActivity(intent)
            }

        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle START_TRACKING", e)
        }
    }

    private fun handleStopTracking() {
        try {
            Log.i(TAG, "STOP_TRACKING received")
            val intent = Intent(this, SmartAlarmService::class.java).apply {
                action = SmartAlarmService.ACTION_STOP_AND_SEND_RESULT
            }
            startService(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle STOP_TRACKING", e)
        }
    }

    companion object {
        private const val TAG = "WatchListenerService"
        private const val PATH_START_TRACKING = "/START_TRACKING"
        private const val PATH_STOP_TRACKING = "/STOP_TRACKING"
    }
}