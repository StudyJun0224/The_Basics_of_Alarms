package com.example.sleeptandard_mvp_demo

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.sleeptandard_mvp_demo.service.SmartAlarmService

class PermissionActivity : ComponentActivity() {

    private var targetAlarmTime: Long = 0L
    private var hasBatterySettingsLaunched = false  // 배터리 설정 화면을 실행했는지 여부

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
        
        // onCreate에서는 배터리 최적화만 확인
        // 권한 확인은 onResume에서 처리 (배터리 설정 후 돌아올 때도 체크하기 위해)
        checkAndRequestBatteryOptimization()
    }
    
    override fun onResume() {
        super.onResume()
        
        // 배터리 설정 화면을 실행했다면, 돌아왔을 때 결과 확인
        if (hasBatterySettingsLaunched) {
            if (isBatteryOptimizationIgnored()) {
                // 배터리 최적화 완료됨 -> 권한 확인
                Log.i(TAG, "✅ Battery optimization granted. Checking permissions...")
                checkAndRequestPermissions()
            } else {
                // 배터리 최적화가 안 되어 있음 -> 재시도 다이얼로그 표시
                Log.w(TAG, "⚠️ Battery optimization not granted by user")
                showBatteryOptimizationRetryDialog()
            }
        }
    }

    /**
     * 배터리 최적화 재시도 다이얼로그 표시
     */
    private fun showBatteryOptimizationRetryDialog() {
        AlertDialog.Builder(this)
            .setTitle("배터리 최적화 예외 필요")
            .setMessage("앱이 백그라운드에서 센서 데이터를 수집하려면 배터리 최적화 예외가 필요합니다.\n\n다시 시도하시겠습니까?")
            .setCancelable(false)  // 뒤로가기로 닫을 수 없음
            .setPositiveButton("다시 시도") { dialog, _ ->
                dialog.dismiss()
                Log.i(TAG, "User requested retry for battery optimization")
                // 플래그 리셋하여 다시 시도할 수 있도록
                hasBatterySettingsLaunched = false
                checkAndRequestBatteryOptimization()
            }
            .setNegativeButton("종료") { dialog, _ ->
                dialog.dismiss()
                Log.i(TAG, "User cancelled battery optimization setup")
                Toast.makeText(this, "배터리 최적화가 필요합니다", Toast.LENGTH_SHORT).show()
                finish()
            }
            .show()
    }
    
    /**
     * 배터리 최적화 예외 상태 확인 및 요청
     */
    private fun checkAndRequestBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isBatteryOptimizationIgnored()) {
                Log.i(TAG, "Requesting battery optimization exception")
                try {
                    // 배터리 설정 화면을 실행하기 직전에 플래그 설정
                    hasBatterySettingsLaunched = true
                    
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open battery optimization settings", e)
                    Toast.makeText(this, "배터리 설정을 열 수 없습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                // 이미 배터리 최적화 예외 설정됨 -> 권한 확인으로
                Log.i(TAG, "Battery optimization already ignored")
                checkAndRequestPermissions()
            }
        } else {
            // Android M 미만은 배터리 최적화 기능 없음 -> 권한 확인으로
            checkAndRequestPermissions()
        }
    }
    
    /**
     * 배터리 최적화 예외 상태 확인
     */
    private fun isBatteryOptimizationIgnored(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
            powerManager.isIgnoringBatteryOptimizations(packageName)
        } else {
            true
        }
    }
    
    /**
     * 런타임 권한 확인 및 요청
     */
    private fun checkAndRequestPermissions() {
        if (checkPermissions()) {
            startTrackingService()
        } else {
            Log.i(TAG, "Requesting runtime permissions")
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun checkPermissions(): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun startTrackingService() {
        Log.i(TAG, "All requirements met. Starting SmartAlarmService")
        val serviceIntent = Intent(this, SmartAlarmService::class.java).apply {
            putExtra(SmartAlarmService.EXTRA_TARGET_TIME, targetAlarmTime)
            action = SmartAlarmService.ACTION_START_TRACKING
        }
        startForegroundService(serviceIntent)
        finish() // 역할 끝났으니 종료
    }
    
    companion object {
        private const val TAG = "PermissionActivity"
    }
}