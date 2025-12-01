package com.example.sleeptandard_mvp_demo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler
import com.example.sleeptandard_mvp_demo.Component.AppNav
import com.example.sleeptandard_mvp_demo.ui.theme.Sleeptandard_MVP_DemoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 권한 설정 여부 확인. 안되어 있으면 설정 창으로
        var scheduler = AlarmScheduler(applicationContext)
        scheduler.confirmSetExactAlarms()

        enableEdgeToEdge()
        setContent {
            Sleeptandard_MVP_DemoTheme {
                AppNav(scheduler)
            }
        }
    }
}
