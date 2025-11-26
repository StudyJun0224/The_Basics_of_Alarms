package com.example.sleeptandard_mvp_demo.Screen

import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.sleeptandard_mvp_demo.ViewModel.AlarmViewModel

import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.chargemap.compose.numberpicker.AMPMHours
import com.chargemap.compose.numberpicker.Hours
import com.chargemap.compose.numberpicker.HoursNumberPicker
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmDay
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Switch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.sleeptandard_mvp_demo.ClassFile.Alarm
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler

@Composable
fun SettingAlarmScreen(
    viewModel: AlarmViewModel,
    onClickConfirm: () -> Unit,
){
    var selectedHour by remember { mutableIntStateOf(8) }
    var selectedMinute by remember { mutableIntStateOf(30) }
    var selectedIsAm by remember { mutableStateOf(true) }
    var selectedDays by remember { mutableStateOf(setOf<AlarmDay>()) }
    var selectedRingtoneUri by remember { mutableStateOf("") }
    var selectedVibrationEnabled by remember { mutableStateOf(true) }

    val context = LocalContext.current
    val scheduler = remember { AlarmScheduler(context) }

    val allDays = listOf(
        AlarmDay.MON,
        AlarmDay.TUE,
        AlarmDay.WED,
        AlarmDay.THU,
        AlarmDay.FRI,
        AlarmDay.SAT,
        AlarmDay.SUN
    )

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(top = 40.dp)
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp)
        ){
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
                ) {
                Button(
                onClick = {
                    val newAlarm: Alarm = viewModel.addAlarm(
                        selectedHour, selectedMinute, selectedIsAm, days = selectedDays, ringtoneUri = selectedRingtoneUri, vibrationEnabled = selectedVibrationEnabled)
                    scheduler.schedule(newAlarm)
                    onClickConfirm()
                } ) {
                Text("확인")
            } }

            TimeAmPmPicker(onTimeChange = {hour12, minute, isAm ->
                selectedHour = hour12
                selectedMinute = minute
                selectedIsAm = isAm
            })

            // 알람 예정시간
            Text(text = earlyWakeUpTime(selectedIsAm, selectedHour, selectedMinute))

            // 요일설정
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                allDays.forEach { day ->
                    FilterChip(
                        selected = day in selectedDays,
                        onClick = {
                            selectedDays =
                                if (day in selectedDays) {
                                    selectedDays - day
                                } else {
                                    selectedDays + day
                                }
                        },
                        label = { Text(day.label) }
                    )
                }
            }

            // 알람음 설정
            // 현재 컨텍스트
            val context = LocalContext.current

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
                val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply { // 추가적으로 설정합니다
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)   // 링톤 타입 = 알람
                    putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, "알람음 선택")                // 링톤 설정창 제목
                    putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,                               // 기존 선택 알람 설정
                        selectedRingtoneUri.let { Uri.parse(it) })
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
        }
    }
}


@Preview
@Composable
fun SettingAlarmScreenPreview(){
    val dummyAlarmViewModel: AlarmViewModel = viewModel()
    val dummyAlarmDaySet: Set<AlarmDay> = setOf(AlarmDay.MON, AlarmDay.THU, AlarmDay.SUN)
    dummyAlarmViewModel.addAlarm(5,30,true, dummyAlarmDaySet, "", true)
    SettingAlarmScreen(viewModel(), onClickConfirm = {})
}

fun earlyWakeUpTime(isAm:Boolean, hour:Int, minute: Int): String{
    var earlyTotalMinute: Int = (hour * 60 + minute) - 90

    val ampm: String = if(isAm && earlyTotalMinute < 0) "오후" else if ( !isAm && earlyTotalMinute < 0) "오전"
    else if (isAm && earlyTotalMinute >= 0) "오전" else "오후"

    if(earlyTotalMinute < 0) earlyTotalMinute += 12 * 60

    return String.format(
        "%s %d:%02d ~ %s %d:%02d 사이에 알람",
        ampm, earlyTotalMinute/60, earlyTotalMinute%60, BoolToAmPm(isAm), hour, minute)
}

// 개빡쳐서 만듦
fun BoolToAmPm(isAm: Boolean): String{
    return if(isAm) "오전" else "오후"
}

@Composable
fun TimeAmPmPicker (
    defaultHour12: Int = 8,
    defaultMinute: Int = 30,
    defaultDay: AMPMHours.DayTime = AMPMHours.DayTime.AM,
    onTimeChange: (hour12: Int, minute: Int, isAm: Boolean) -> Unit
){
    var pickerValue = remember {
        mutableStateOf<Hours>(
            AMPMHours(
                defaultHour12,
                defaultMinute,
                defaultDay
            )
        )
    }
    HoursNumberPicker(
        dividersColor = MaterialTheme.colorScheme.primary,
        value = pickerValue.value,
        onValueChange = {
            pickerValue.value = it

            val ampm = it as? AMPMHours ?: return@HoursNumberPicker
            onTimeChange(
                ampm.hours,
                ampm.minutes,
                ampm.dayTime == AMPMHours.DayTime.AM
            )
        },
        hoursDivider = {
            Text(
                modifier = Modifier
                    .width(16.dp)
                    .padding(horizontal = 4.dp),
                text = ":",
                textAlign = TextAlign.Center
            )
        },
        minutesDivider = {
            Spacer(modifier = Modifier.width(8.dp))
        }
    )

}