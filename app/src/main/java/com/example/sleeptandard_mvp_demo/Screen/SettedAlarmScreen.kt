package com.example.sleeptandard_mvp_demo.Screen

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.sleeptandard_mvp_demo.ClassFile.Alarm
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler
import com.example.sleeptandard_mvp_demo.Prefs.AlarmPreferences
import com.example.sleeptandard_mvp_demo.ViewModel.AlarmViewModel

@Composable
fun SettedAlarmScreen(
    alarmViewModel: AlarmViewModel,
    scheduler: AlarmScheduler,
    onTurnAlarmOff : ()-> Unit
){
    val context = LocalContext.current   // ✨ 추가

    val alarm: Alarm = alarmViewModel.alarm

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(40.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
        ) {
            // TODO: settingAlarmScreen에서 시간 계산 함수 가져오기
            Text(getWakeUpTimeRange(alarm.isAm,alarm.hour,alarm.minute))

            Text("워치차고 푹 자요.")

            Button(
                onClick = {
                    onTurnAlarmOff()
                    scheduler.cancel(alarmViewModel.alarm)

                    // 2) SharedPreferences 플래그/값 삭제
                    val alarmPrefs = AlarmPreferences(context)
                    alarmPrefs.clearAlarm()

                    // 3) 네비게이션 처리
                    onTurnAlarmOff()
                }
            ){
                Text("알람 끄기")
            }
        }
    }
}

fun getWakeUpTimeRange(isAm:Boolean, hour:Int, minute: Int): String{
    var earlyTotalMinute: Int = (hour * 60 + minute) - 30

    val ampm: String =
        if(isAm && earlyTotalMinute < 0) "오후"
        else if (isAm && earlyTotalMinute >= 0) "오전"
        else if ( !isAm && earlyTotalMinute < 0) "오전"
        else "오후"

    if(earlyTotalMinute < 0) earlyTotalMinute += 12 * 60

    return String.format(
        "%s %d:%02d ~ %s %d:%02d 사이에 깨워드릴게요\uD83C\uDF1D",
        ampm, earlyTotalMinute/60, earlyTotalMinute%60, getAmPmString(isAm), hour, minute)
}

// 개빡쳐서 만듦
fun getAmPmString(isAm: Boolean): String{
    return if(isAm) "오전" else "오후"
}