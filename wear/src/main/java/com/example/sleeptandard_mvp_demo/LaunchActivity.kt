package com.example.sleeptandard_mvp_demo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.sleeptandard_mvp_demo.service.SmartAlarmService

/**
 * Trampoline Activity - 화면이 꺼진 상태에서 서비스를 안전하게 시작하기 위한 중계 액티비티
 * 
 * 배경:
 * - Wear OS에서 화면이 꺼진 상태에서 서비스를 직접 시작하면 시스템 제한으로 실패할 수 있음
 * - Activity를 통해 화면을 깨우고 서비스를 시작하면 이 문제를 해결할 수 있음
 */
class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "LaunchActivity onCreate() - Screen wake-up initiated")

        // 화면 강제 깨우기 (Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            // Android 7.x 이하 대응
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        // Intent에서 targetAlarmTime 추출
        val targetAlarmTime = intent.getLongExtra(SmartAlarmService.EXTRA_TARGET_TIME, 0L)
        
        if (targetAlarmTime == 0L) {
            Log.w(TAG, "⚠️ No target alarm time provided. Finishing activity.")
            finish()
            return
        }

        // SmartAlarmService 시작
        val serviceIntent = Intent(this, SmartAlarmService::class.java).apply {
            action = SmartAlarmService.ACTION_START_TRACKING
            putExtra(SmartAlarmService.EXTRA_TARGET_TIME, targetAlarmTime)
        }

        try {
            startForegroundService(serviceIntent)
            Log.i(TAG, "✅ SmartAlarmService started successfully via LaunchActivity")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to start SmartAlarmService", e)
        } finally {
            // 즉시 종료 (사용자에게 보이지 않음)
            finish()
        }
    }

    companion object {
        private const val TAG = "LaunchActivity"
    }
}

