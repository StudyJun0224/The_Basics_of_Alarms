package com.example.sleeptandard_mvp_demo.ClassFile

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

class AlarmScheduler(private val context: Context) {

    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        val triggerTime = calculateNextTriggerTime(alarm)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("label", "알람 #${alarm.id}")
            putExtra("ringtoneUri", alarm.ringtoneUri)
            putExtra("vibrationEnabled", alarm.vibrationEnabled)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id, // 알람마다 다른 requestCode
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
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
}