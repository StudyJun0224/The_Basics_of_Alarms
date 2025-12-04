package com.example.sleeptandard_mvp_demo.Screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sleeptandard_mvp_demo.Component.Screen

@Composable
fun ReviewAlarmScreen(
    onSubmit: () -> Unit = {}   // 선택값 전달 콜백
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {

        Text(
            text = "알람은 어땠나요?",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 라디오 버튼 3개
        RadioOptionItem(
            text = "Bad 😵",
            selected = selectedOption == "bad",
            onClick = { selectedOption = "bad" }
        )

        RadioOptionItem(
            text = "So so 😐",
            selected = selectedOption == "soso",
            onClick = { selectedOption = "soso" }
        )

        RadioOptionItem(
            text = "Good 😊",
            selected = selectedOption == "good",
            onClick = { selectedOption = "good" }
        )

        Spacer(modifier = Modifier.height(30.dp))

        // 제출 버튼
        Button(
            onClick = onSubmit ,
            enabled = selectedOption != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("제출하기")
        }
    }
}

@Composable
fun RadioOptionItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Preview
@Composable
fun ReviewAlarmScreenPreview(){
    ReviewAlarmScreen({})
}