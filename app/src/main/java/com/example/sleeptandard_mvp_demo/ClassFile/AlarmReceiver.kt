package com.example.sleeptandard_mvp_demo.ClassFile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 1) 알람 정보 꺼내기
        val label = intent.getStringExtra("label") ?: "알람"
        val ringtoneUriString = intent.getStringExtra("ringtoneUri")
        val vibrationEnabled = intent.getBooleanExtra("vibrationEnabled", true)
         // 2) 벨소리 재생
        val ringtone = try {
            val uri = if (ringtoneUriString != null) {
                Uri.parse(ringtoneUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
            RingtoneManager.getRingtone(context, uri)
        } catch (e: Exception) {
            null
        }
        ringtone?.play()

        // 3) 진동
        if (vibrationEnabled) {
            vibrate(context)
        }

    // (선택) Notification이나 알람화면 Activity 띄우기도 가능
    // 여기선 최소 버전이라 소리+진동만
    }

    private fun vibrate(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(VibratorManager::class.java)
            val vibrator = vibratorManager.defaultVibrator
            val effect = VibrationEffect.createOneShot(
                1000L,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val vibrator =
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createOneShot(
                    1000L,
                    VibrationEffect.DEFAULT_AMPLITUDE
                )
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(1000L)
            }
        }
    }
}
