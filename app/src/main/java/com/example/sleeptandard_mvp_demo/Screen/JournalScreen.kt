package com.example.sleeptandard_mvp_demo.Screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.example.sleeptandard_mvp_demo.Component.AlarmBottomNavBar
import com.example.sleeptandard_mvp_demo.ui.theme.AppIcons

@Composable
fun JournalScreen() {
    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            AlarmBottomNavBar(
                selectedIndex = selectedIndex,
                onSelect = { selectedIndex = it }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(innerPadding)
        ) {
            Row(){
                Text("아직 준비 중인 기능입니다아직 준비 중인 기능입니다")
                Icon(
                    painter = painterResource(AppIcons.JournalSmile),
                    contentDescription = "스마일"
                )
            }

        }

    }
}