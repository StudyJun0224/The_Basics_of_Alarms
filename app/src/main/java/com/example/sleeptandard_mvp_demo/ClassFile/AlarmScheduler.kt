package com.example.sleeptandard_mvp_demo.ClassFile

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    // 알람 기능 받아오기
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {

        // 알람이 실제 울리는 시간 계산
        val triggerTime = calculateNextTriggerTime(alarm)

        // BroadcastReceiver에게 전달할 Intent 정의
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("label", "알람 #${alarm.id}")
            putExtra("ringtoneUri", alarm.ringtoneUri)
            putExtra("vibrationEnabled", alarm.vibrationEnabled)
        }

        // 위에만든 intent를 시스템이 대신 실행해주는 PendingIntent
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id, // 알람마다 다른 requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE   // 같은 id면 Extras만 업데이트 or 만든 뒤에는 변경 불가
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
        /*// 🔸 사용기기가 안드로이드 12(S, API 31) 이상이면 "정확한 알람" 권한을 확인함 / 설정 화면 사용
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                // 사용자가 "정확한 알람" 권한을 아직 안 줌 → 설정 화면으로 보냄
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return
            }

            // 정확한 알람 허용된 경우
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            // 🔹 안드로이드 11 이하에서는 원래대로 그냥 써도 됨 (권한 필요 없음)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }*/
    }

    private fun calculateNextTriggerTime(alarm: Alarm): Long {
        val now = Calendar.getInstance()

        val cal = Calendar.getInstance().apply {
            // isAm, hour를 24시간제로 변환
            var h = alarm.hour % 12
            if (!alarm.isAm) h += 12
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // 오늘 시간이 이미 지났으면 +1일
        if (cal.before(now)) {
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }

        // 요일 Set까지 적용하려면 여기서 alarm.days 보고
        // 가장 가까운 요일로 이동하는 로직을 넣으면 됨(나중에 확장)

        return cal.timeInMillis
    }

    fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    // 나중에 앱 시작시 사용할 사용권한 확인 함수
    fun confirmSetExactAlarms(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if (!alarmManager.canScheduleExactAlarms()) {
                // 사용자가 "정확한 알람" 권한을 아직 안 줌 → 설정 화면으로 보냄
                // TODO: SCHEDULE_EXACT_ALARM 대신 USE_EXACT_ALARM 고려
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(settingsIntent)
                return
            }
        }
    }
}