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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview

import androidx.core.net.toUri

import com.chargemap.compose.numberpicker.AMPMHours

import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler
import com.example.sleeptandard_mvp_demo.Component.AlarmBottomNavBar
import com.example.sleeptandard_mvp_demo.Component.ConfirmButton
import com.example.sleeptandard_mvp_demo.Component.CustomTimePicker
import com.example.sleeptandard_mvp_demo.Component.OptionsSection
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
    var alarmName by remember {mutableStateOf("")}

    LaunchedEffect(alarmViewModel.alarm.ringtoneUri) {
        val uriStr = alarmViewModel.alarm.ringtoneUri
        if (uriStr.isNotBlank()) {
            val uri = uriStr.toUri()
            val ringtone = RingtoneManager.getRingtone(context, uri)
            alarmName = ringtone?.getTitle(context) ?: "소리 없음"
            selectedRingtoneUri = uriStr
        } else {
            alarmName = "소리 없음"
        }
    }

    // 알림음 설정 화면 Activity의 Result 받았을 때 로직
    val ringtonePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri = result.data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            if (uri != null) {
                selectedRingtoneUri = uri.toString()   // state에 저장
                // ✅ 표시할 이름 업데이트
                val ringtone = RingtoneManager.getRingtone(context, uri)
                alarmName = ringtone?.getTitle(context) ?: "소리 없음"
            }else {
                // 사용자가 '없음' 선택했거나 취소 케이스 대응
                selectedRingtoneUri = ""
                alarmName = "소리 없음"
            }
        }
    }

    Scaffold(
        bottomBar = {
            AlarmBottomNavBar(
                onHomeClick = {},
                onScheduleClick = {},
                onSettingsClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier.height(92.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
            ){
                CustomTimePicker(
                    defaultHour12 = alarmViewModel.alarm.hour,
                    defaultMinute = alarmViewModel.alarm.minute,
                    defaultIsAm = alarmViewModel.alarm.isAm,
                    onTimeChange = {hour12, minute, isAm ->
                        selectedHour = hour12
                        selectedMinute = minute
                        selectedIsAm = isAm
                    }
                )
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp)
            )

            OptionsSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 35.dp),

                // 링톤 설정
                onSoundClick = {
                    val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
                    .apply{
                        // 추가적으로 설정합니다
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)   // 링톤 타입 = 알람
                        putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알람음 선택")                // 링톤 설정창 제목
                        putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,                               // 기존 선택 알람 설정
                            selectedRingtoneUri.toUri()
                        )
                    }
                    ringtonePickerLauncher.launch(intent)},

                // 진동 토글
                onVibrationClick = { selectedVibrationEnabled = !selectedVibrationEnabled },
                checked = selectedVibrationEnabled,
                onCheckedChange = {selectedVibrationEnabled = it},
                alarmName = alarmName
            )

            Spacer(modifier = Modifier.height(64.dp))

            ConfirmButton(
                modifier = Modifier
                    .fillMaxWidth(193f/350f),
                onClick = {
                    onClickSetting()
                    alarmViewModel.saveAlarm(
                        selectedHour, selectedMinute, selectedIsAm, selectedRingtoneUri, selectedVibrationEnabled)
                    scheduler.schedule(alarmViewModel.alarm)

                    val triggerTime = scheduler.getTriggerTime()

                    // 알람뷰모델에 triggerTime 보내기
                    alarmViewModel.startSleepTracking(triggerTime)

                    // 여기서 알람 정보를 디스크에 저장
                    val alarmPrefs = AlarmPreferences(context)
                    alarmPrefs.saveAlarm(alarmViewModel.alarm)
                }
            )

        }
    }
}

@Preview
@Composable
fun HomeScreenPreview() {

    var bool = true
    var h = 6
    var m = 25
    var i = true

    Scaffold(
        bottomBar = {
            AlarmBottomNavBar(
                onHomeClick = {},
                onScheduleClick = {},
                onSettingsClick = {}
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(
                modifier = Modifier.height(92.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 36.dp)
            ) {
                CustomTimePicker(
                    onTimeChange = { hour12, minute, isAm ->
                        h = hour12
                        m = minute
                        i = isAm
                    }
                )
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp)
            )

            OptionsSection(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 35.dp),

                // 링톤 설정
                onSoundClick = {},

                // 진동 토글
                onVibrationClick = { },
                checked = bool,
                onCheckedChange = { bool = it },
                alarmName = "alarmName"
            )

            Spacer(modifier = Modifier.height(64.dp))

            ConfirmButton(
                modifier = Modifier
                    .fillMaxWidth(193f / 350f),
                onClick = {}
            )

        }
    }
}


/********************** UI 변경 전 **********************/

/*
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

                val triggerTime = scheduler.getTriggerTime()

                // TODO: 알람뷰모델에 triggerTime 보내기
                alarmViewModel.startSleepTracking(triggerTime)

                // 여기서 알람 정보를 디스크에 저장
                val alarmPrefs = AlarmPreferences(context)
                alarmPrefs.saveAlarm(alarmViewModel.alarm)
            } )
        {
            Text("GTS")
        }
    }
}
*/