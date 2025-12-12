package com.example.sleeptandard_mvp_demo.Component

import android.content.Intent
import android.widget.Button
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.sleeptandard_mvp_demo.ui.theme.AppIcons
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.compose.runtime.MutableState

@Composable
fun AlarmBottomNavBar(
    onHomeClick: () -> Unit,
    onScheduleClick: () -> Unit,
    onSettingsClick: () -> Unit,
){
    NavigationBar {
        NavigationBarItem(
            selected = true,
            onClick = onHomeClick,
            icon = {
                Icon(
                    painter = painterResource(AppIcons.NavAlarm),
                    contentDescription = "알람"
                ) },
            label = { Text("알람") }
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
    onCheckedChange: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
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
                style = MaterialTheme.typography.bodyMedium
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
    onCheckedChange: () -> Unit
    ) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color(0xFFB39DDB)), // Figma 보라색 라인 느낌
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