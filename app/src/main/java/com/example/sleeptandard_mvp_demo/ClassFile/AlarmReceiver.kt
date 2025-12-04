package com.example.sleeptandard_mvp_demo.ClassFile


import android.content.BroadcastReceiver
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.sleeptandard_mvp_demo.AlarmRingActivity
import com.example.sleeptandard_mvp_demo.R

private const val ALARM_CHANNEL_ID = "alarm_channel"

// 소리/진동을 Activity에서도 끌 수 있도록 전역으로 관리하는 객체
object AlarmPlayer {
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null

    fun start(context: Context, ringtoneUriString: String?, vibrationEnabled: Boolean) {
        // 🔔 소리
        val uri = try {
            if (ringtoneUriString != null) {
                Uri.parse(ringtoneUriString)
            } else {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            }
        } catch (e: Exception) {
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        }

        ringtone = RingtoneManager.getRingtone(context, uri)
        ringtone?.play()

        // 📳 진동
        if (vibrationEnabled) {
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(VibratorManager::class.java)
                vm.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val effect = VibrationEffect.createWaveform(
                    longArrayOf(0, 600, 400), // 0ms 대기, 600ms 진동, 400ms 쉼
                    0 // 반복
                )
                try{
                vibrator?.vibrate(effect)
                    Log.d("vibration","성공")
                }catch (e: Exception){
                    Log.d("vibration","실패: ${e}")
                }
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 600, 400), 0)
            }
        }
    }

    fun stop() {
        ringtone?.stop()
        ringtone = null
        vibrator?.cancel()
        vibrator = null
    }
}

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 알람 정보 (없으면 기본값 사용)
        val label = intent.getStringExtra("label") ?: "알람"
        val ringtoneUriString = intent.getStringExtra("ringtoneUri")
        val vibrationEnabled = intent.getBooleanExtra("vibrationEnabled", true)
        val alarmId = intent.getIntExtra("alarmId", 0)

        // 1) 소리/진동 시작 (Activity가 안 떠도 최소한 울리게)
        AlarmPlayer.start(context, ringtoneUriString, vibrationEnabled)

        // 2) 알람 채널 생성
        createAlarmChannel(context)

        // 3) 전체화면으로 띄울 Activity 인텐트
        val fullScreenIntent = Intent(context, AlarmRingActivity::class.java).apply {
            putExtra("alarmId", alarmId)
            putExtra("label", label)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId,
            fullScreenIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // 4) 사용자가 알림을 탭했을 때 열리는 contentIntent 도 같이 설정
        val contentPendingIntent = fullScreenPendingIntent

        // 5) Notification 빌드 (ALARM 카테고리 + HIGH / fullScreenIntent)
        val notification = NotificationCompat.Builder(context, ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)  // 프로젝트 아이콘으로 바꿔도 됨
            .setContentTitle(label)
            .setContentText("알람이 울리고 있어요")
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true) // 스와이프로 안 없애지게
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentPendingIntent)
            // 🔥 여기서 full-screen 요청 (USE_FULL_SCREEN_INTENT + 사용자 설정 ON일 때 동작)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .build()

        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(alarmId, notification)
    }

    private fun createAlarmChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(NotificationManager::class.java)
            val channel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "알람 채널",
                NotificationManager.IMPORTANCE_HIGH   // 🔥 HIGH 채널
            ).apply {
                description = "알람이 울릴 때 사용하는 채널입니다."
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            nm.createNotificationChannel(channel)
        }
    }
}