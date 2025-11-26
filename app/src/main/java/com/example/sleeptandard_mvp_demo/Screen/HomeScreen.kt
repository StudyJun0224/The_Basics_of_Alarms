package com.example.sleeptandard_mvp_demo.Screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

import com.example.sleeptandard_mvp_demo.ClassFile.Alarm
import com.example.sleeptandard_mvp_demo.ViewModel.AlarmViewModel

@Composable
fun HomeScreen(
    alarmViewModel: AlarmViewModel,
    onClickSetting: ()-> Unit
){

    Surface(modifier = Modifier
        .fillMaxSize()
        .padding(top = 40.dp)) {
        Column(modifier = Modifier.fillMaxWidth()
            .padding(10.dp)){
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
                ){

                Button(onClick = {}) {
                    Text("편집")
                }

                Text(
                    text = "알람의 정석",
                    style = MaterialTheme.typography.headlineSmall
                )

                Button(onClick = onClickSetting) {
                    Icon(imageVector = Icons.Filled.AddCircle,
                        contentDescription = null)

                }
            }
            LazyColumn(
                modifier = Modifier.padding(vertical = 4.dp)
                    .fillMaxWidth()
                    .fillMaxHeight()
            ){
                items(items = alarmViewModel.alarms){
                        item -> AlarmList(alarm = item, onToggle = {alarmViewModel.toggleAlarm(item.id)}, onDelete = {alarmViewModel.deleteAlarm(item.id)})
                }
            }
        }

    }
}

@Composable
fun NothingToSee(){
    Surface{
        Text("Set your alarm!")
    }
}

@Composable
fun AlarmList(
    alarm: Alarm,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
){
    val bgColor =
        if (alarm.isOn) {
            MaterialTheme.colorScheme.primary
        } else {
            // 꺼진 알람은 더 연한 색 (예: primaryContainer, 또는 alpha 낮추기)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            // 또는 MaterialTheme.colorScheme.surfaceVariant
        }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = bgColor
        ),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp),
        // 추가 구현
        onClick = { /* 알람 편집 */ }
    ){

        CardContent(alarm = alarm, onToggle = onToggle, onDelete = onDelete)
    }
}

@Composable
fun CardContent(
    alarm: Alarm,
    onToggle: ()-> Unit,
    onDelete: ()-> Unit
){
    val ampm: String = if(alarm.isAm) "오전" else "오후"
    val daysText = if (alarm.days.isEmpty()) {
        " "
    } else {
        alarm.days
            .sortedBy { it.ordinal }
            .joinToString(separator = " ") { it.label }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement
                .SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ){
            Switch(
                checked = alarm.isOn,
                onCheckedChange = {
                    onToggle()
                }
            )
            Row(modifier = Modifier.padding(5.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = ampm,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(text = daysText, style = MaterialTheme.typography.bodySmall)

                Text(
                    text = String.format("%d:%02d", alarm.hour, alarm.minute),
                    style = MaterialTheme.typography.headlineSmall
                )}


            TextButton(onClick = onDelete) {
                Text("삭제")
            }
        }
    }
}