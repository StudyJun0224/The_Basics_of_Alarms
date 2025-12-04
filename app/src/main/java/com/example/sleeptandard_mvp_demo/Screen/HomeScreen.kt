package com.example.sleeptandard_mvp_demo.Screen

import android.app.Activity
import android.content.Context

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import android.media.RingtoneManager

import android.net.Uri

import android.content.Intent
import androidx.compose.ui.platform.LocalContext

import androidx.core.net.toUri

import com.chargemap.compose.numberpicker.AMPMHours

import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler
import com.example.sleeptandard_mvp_demo.Component.TimeAmPmPicker
import com.example.sleeptandard_mvp_demo.Prefs.AlarmPreferences
import com.example.sleeptandard_mvp_demo.ViewModel.AlarmViewModel

@Composable
fun HomeScreen(
    alarmViewModel: AlarmViewModel,
    scheduler: AlarmScheduler,
    onClickSetting: ()-> Unit,
    isAm : Boolean = true,
    hour12 : Int = 8,
    minute : Int = 30,
){
    val context = LocalContext.current

    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    var selectedIsAm by remember { mutableStateOf(true) }
    var selectedRingtoneUri by remember { mutableStateOf("") }
    var selectedVibrationEnabled by remember { mutableStateOf(true) }

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(top = 40.dp)) {
        Column(modifier = Modifier.fillMaxWidth()
            .padding(10.dp)
        )
        {
            TimeAmPmPicker(
                defaultHour12 = alarmViewModel.alarm.hour,
                defaultMinute = alarmViewModel.alarm.minute,
                defaultDay =
                    if(alarmViewModel.alarm.isAm)
                        AMPMHours.DayTime.AM
                    else AMPMHours.DayTime.PM,
                onTimeChange = {hour12, minute, isAm ->
                selectedHour = hour12
                selectedMinute = minute
                selectedIsAm = isAm
                }
            )
            // 알람음 설정
            // Activity Result 결과 받았을 때 로직
            val ringtonePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.StartActivityForResult()
            ) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
                    if (uri != null) {
                        selectedRingtoneUri = uri.toString()   // state에 저장
                    }
                }
            }

            // 알람음 선택 버튼
            Button(onClick = {
                // 링톤 픽커 열기
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                    .apply{
                        // 추가적으로 설정합니다
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)   // 링톤 타입 = 알람
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알람음 선택")                // 링톤 설정창 제목
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,                               // 기존 선택 알람 설정
                            selectedRingtoneUri.toUri()
                        )
                    }
                ringtonePickerLauncher.launch(intent)
            }) {
                Text("알람음 선택")
            }

            // 진동 선택
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("진동")
                Switch(
                    checked = selectedVibrationEnabled,
                    onCheckedChange = { selectedVibrationEnabled = it }
                )
            }

            Button(
                onClick = {
                    onClickSetting()
                    alarmViewModel.saveAlarm(
                        selectedHour, selectedMinute, selectedIsAm, selectedRingtoneUri, selectedVibrationEnabled)
                    scheduler.schedule(alarmViewModel.alarm)

                    // 여기서 알람 정보를 디스크에 저장
                    val alarmPrefs = AlarmPreferences(context)
                    alarmPrefs.saveAlarm(alarmViewModel.alarm)
                } )
            {
                Text("GTS")
            }
        }



    }
}
