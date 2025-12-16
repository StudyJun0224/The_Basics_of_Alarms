package com.example.sleeptandard_mvp_demo.Component

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
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 사운드 선택 영역 (더 넓게)
        SoundOptionCard(
            modifier = Modifier.weight(2f),
            onClick = onSoundClick
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
    onClick: ()->Unit
) {
    Surface(
        modifier = modifier
            .height(68.dp)
            .width(123.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(Icons.Default.VolumeUp, contentDescription = null)
            Text(
                text = "Indigo Puff",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null)
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
    Surface(
        modifier = modifier
            .height(68.dp)
            .width(123.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Default.Vibration, contentDescription = null)
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
        Text("완료")
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
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
                    .padding(vertical = 8.dp)
            )

            OptionsSection(
                modifier = Modifier
                    .fillMaxWidth(),
                onSoundClick = {},
                onCheckedChange = { wtf = it},
                onVibrationClick = {},
                checked = wtf
            )

            Spacer(modifier = Modifier.height(16.dp))

            ConfirmButton(
                modifier = Modifier
                    .fillMaxWidth(193f / 350f), // 가운데 둥근 버튼
                onClick = {}
            )
        }
    }
}