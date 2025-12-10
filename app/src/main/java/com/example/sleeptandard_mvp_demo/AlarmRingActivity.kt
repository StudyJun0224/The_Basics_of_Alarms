package com.example.sleeptandard_mvp_demo

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmPlayer
import com.example.sleeptandard_mvp_demo.Prefs.AlarmPreferences
import com.example.sleeptandard_mvp_demo.ui.theme.Sleeptandard_MVP_DemoTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class AlarmRingActivity : ComponentActivity() {

    private var alarmId: Int = 0
    private var label: String = "알람"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try{

        } catch(e:Exception){
            Log.d("WTF", "WTF: ${e}")
        }
        val alarmPrefs = AlarmPreferences(this)
        alarmId = intent.getIntExtra("alarmId", 0)
        label = intent.getStringExtra("label") ?: "알람"

        // 🔥 잠금 화면 위에 띄우고, 화면 켜기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }

        setContent {
            Sleeptandard_MVP_DemoTheme {
                AlarmRingScreen(
                    label = label,
                    onStop = {
                        stopAlarmAndFinish()
                        try {
                            alarmPrefs.clearAlarm()
                        }catch (e: Exception){
                            Log.d("clearPrefs", "WTF: ${e}")
                        }

                    }
                )
            }
        }
    }

    private fun stopAlarmAndFinish() {
        // 1) 소리/진동 정지
        AlarmPlayer.stop()

        // 2) 알림 제거
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(alarmId)

        // 3) MainActivity로 넘어가면서 알람 리뷰 화면에서 부터 시작하도록 요청
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("startDestination", "reviewAlarm") // Screen.AfterAlarm.route 값
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
        }
        startActivity(intent)

        // 3) 화면 닫기
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 혹시 남아있을지 모를 소리/진동 정리
        AlarmPlayer.stop()
    }
}

@Composable
fun AlarmRingScreen(
    label: String,
    onStop: () -> Unit
) {
    val currentTime = remember {
        LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "알람이 울리고 있어요",
            color = Color.White,
            fontSize = 24.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = currentTime,
            color = Color.White,
            fontSize = 72.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onStop) {
            Text("알람 끄기")
        }
    }
}

@Preview
@Composable
fun AlarmRingScreenPreview(){
    AlarmRingScreen("preview", {})
}