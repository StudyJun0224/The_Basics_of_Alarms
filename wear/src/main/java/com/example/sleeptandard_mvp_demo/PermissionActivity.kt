package com.example.sleeptandard_mvp_demo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.sleeptandard_mvp_demo.service.SmartAlarmService

class PermissionActivity : ComponentActivity() {

    private var targetAlarmTime: Long = 0L

    // [중요] 런타임 권한만 포함. REQUEST_IGNORE_BATTERY_OPTIMIZATIONS는 별도 Intent로 처리
    // Foreground Service 환경에서는 BODY_SENSORS만으로 충분 (BODY_SENSORS_BACKGROUND 불필요)
    private val requiredPermissions = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION,
        Manifest.permission.POST_NOTIFICATIONS
        // 주의: REQUEST_IGNORE_BATTERY_OPTIMIZATIONS는 여기에 포함하지 말것!
        // 이 권한은 Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS Intent로 처리해야 함
    )

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            startTrackingService()
        } else {
            Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 인텐트로 전달받은 목표 시간 저장
        targetAlarmTime = intent.getLongExtra(SmartAlarmService.EXTRA_TARGET_TIME, 0L)

        if (checkPermissions()) {
            startTrackingService()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun checkPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startTrackingService() {
        val serviceIntent = Intent(this, SmartAlarmService::class.java).apply {
            putExtra(SmartAlarmService.EXTRA_TARGET_TIME, targetAlarmTime)
            action = SmartAlarmService.ACTION_START_TRACKING
        }
        startForegroundService(serviceIntent)
        finish() // 역할 끝났으니 종료
    }
}