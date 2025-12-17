package com.example.sleeptandard_mvp_demo.Component

import android.R
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sleeptandard_mvp_demo.ui.theme.AppIcons
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.material3.Scaffold
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.chargemap.compose.numberpicker.AMPMHours

@Composable
fun AlarmBottomNavBar(
    onHomeClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onSettingsClick: () -> Unit,
){
    NavigationBar() {
        NavigationBarItem(
            selected = true,
            onClick = onHomeClick,
            icon = {
                Icon(
                    painter = painterResource(AppIcons.NavAlarm),
                    contentDescription = "알람"
                )
            },
            label = {Text("알람")}
        )
        NavigationBarItem(
            selected = false,
            onClick = onScheduleClick,
            icon = {
                Icon(
                    painter = painterResource(AppIcons.NavJournal),
                    contentDescription = "일지") },
            label = { Text("일지") }
        )
        NavigationBarItem(
            selected = false,
            onClick = onSettingsClick,
            icon = {
                Icon(
                    painter = painterResource(AppIcons.NavSettings),
                    contentDescription = "설정") },
            label = { Text("설정") }
        )
    }
}

@Composable
fun OptionsSection(
    modifier: Modifier = Modifier,
    onSoundClick: ()->Unit,
    onVibrationClick: ()->Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    alarmName: String
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 사운드 선택 영역 (더 넓게)
        SoundOptionCard(
            modifier = Modifier.weight(2f),
            onClick = onSoundClick,
            alarmName = alarmName,
        )

        // 진동 토글 영역 (조금 좁게)
        VibrationOptionCard(
            modifier = Modifier.weight(1f),
            onClick = onVibrationClick,
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
fun SoundOptionCard(
    modifier: Modifier = Modifier,
    onClick: ()->Unit,
    alarmName: String
) {
    val isNone = alarmName == "소리 없음"

    val containerColor =
        if (isNone) MaterialTheme.colorScheme.surfaceDim
        else MaterialTheme.colorScheme.surface

    // ✅ 아이콘/텍스트 색도 상태에 따라 변경
    val iconTint =
        if (isNone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.onSurface

    val textColor =
        if (isNone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
        else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .height(68.dp)
            .width(123.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        tonalElevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                painter = painterResource(AppIcons.HomeVolume),
                contentDescription = "알람음 설정",
                tint = iconTint
            )
            Text(
                text = alarmName,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 15.sp
                ),
                color = textColor
            )
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = iconTint
            )
        }
    }
}

@Composable
fun VibrationOptionCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
    ) {
    val isNone = !checked

    val containerColor =
        if (isNone) MaterialTheme.colorScheme.surfaceDim
        else MaterialTheme.colorScheme.surface

    // ✅ 아이콘/텍스트 색도 상태에 따라 변경
    val iconTint =
        if (isNone) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
        else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .height(68.dp)
            .width(123.dp),
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                painter = painterResource(AppIcons.HomeVibrate),
                contentDescription = "진동 설정",
                tint = iconTint
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Composable
fun ConfirmButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .height(56.dp),
        shape = RoundedCornerShape(28.dp),
        onClick = onClick,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = "GTS",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 18.sp
            )
        )
    }
}

@Preview
@Composable
fun HomeScreenPreiview(){
    var wtf = true
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
                TimeAmPmPicker(
                    defaultHour12 = 6,
                    defaultMinute = 12,
                    defaultDay = AMPMHours.DayTime.AM,
                    onTimeChange = { hour12, minute, isAm ->

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
                onSoundClick = {},
                onCheckedChange = { wtf = it},
                onVibrationClick = {},
                checked = wtf,
                alarmName = "어쩔 알람"
            )

            Spacer(modifier = Modifier.height(64.dp))

            ConfirmButton(
                modifier = Modifier
                    .fillMaxWidth(193f / 350f)
                    .height(68.dp), // 가운데 둥근 버튼
                onClick = {}
            )
        }
    }
}