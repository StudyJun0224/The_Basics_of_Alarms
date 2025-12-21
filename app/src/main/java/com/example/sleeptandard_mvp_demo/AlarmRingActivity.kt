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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmPlayer
import com.example.sleeptandard_mvp_demo.Prefs.AlarmPreferences
import com.example.sleeptandard_mvp_demo.ui.theme.Sleeptandard_MVP_DemoTheme
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import com.example.sleeptandard_mvp_demo.ui.theme.AlarmBackground
import com.example.sleeptandard_mvp_demo.ui.theme.AppIcons
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

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

        /* Not using : 잠금화면 위에 안띄울거임
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
        */


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
            .background(AlarmBackground)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(238.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentTime,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 80.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight(500),
                    color = Color.White
                )
            )
            Icon(
                painter = painterResource(AppIcons.RingBar),
                contentDescription = "",
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(250.dp))

        /*
        Button(onClick = onStop) {
            Text("알람 끄기")
        }
         */

        SwipeToStopButton(
            text = "피드백",   // 사진처럼
            onComplete = {
                Log.d("Swipe", "COMPLETED!")
                onStop() },
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .height(56.dp)
        )
    }
}

@Composable
fun SwipeToStopButton(
    text: String,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    height: Dp = 67.dp,
    thumbSize: Dp = 57.dp,
    horizontalPadding: Dp = 6.dp,
    completeThreshold: Float = 0.92f,
) {
    val density = LocalDensity.current
    val thumbPx = with(density) { thumbSize.toPx() }
    val padPx = with(density) { horizontalPadding.toPx() }

    var dragX by remember { mutableFloatStateOf(0f) }
    var completed by remember { androidx.compose.runtime.mutableStateOf(false) }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier) {
        val trackWidthPx = with(density) { maxWidth.toPx() }
        val maxDrag = max(0f, trackWidthPx - thumbPx - padPx * 2)

        val animatedX by animateFloatAsState(dragX, label = "thumbX")
        val progress = if (maxDrag == 0f) 0f else (animatedX / maxDrag).coerceIn(0f, 1f)

        // ✅ 트랙 전체가 드래그를 받게!
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.White.copy(alpha = 0.18f))
                .pointerInput(completed, maxDrag) {
                    detectHorizontalDragGestures(
                        onDragStart = { Log.d("Swipe", "drag start") },
                        onHorizontalDrag = { _, dragAmount ->
                            if (completed) return@detectHorizontalDragGestures
                            dragX = (dragX + dragAmount).coerceIn(0f, maxDrag)
                            Log.d("Swipe", "dragX=$dragX / maxDrag=$maxDrag")
                        },
                        onDragEnd = {
                            val endProgress = if (maxDrag == 0f) 0f else (dragX / maxDrag)
                            Log.d("Swipe", "drag end progress=$endProgress")

                            if (completed) return@detectHorizontalDragGestures
                            if (endProgress >= completeThreshold) {
                                completed = true
                                dragX = maxDrag
                                Log.d("Swipe", "COMPLETED!")
                                onComplete()
                            } else {
                                dragX = 0f
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 18.sp,
                    color = Color.White
                )
            )

            // thumb (흰 원) - 이건 이제 "표시만"
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = horizontalPadding)
                    .offset { IntOffset(animatedX.roundToInt(), 0) }   // ✅ 여기!
                    .size(thumbSize)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(AppIcons.ArrowRight),
                    contentDescription = null,
                    tint = Color.Black
                )
            }
        }
    }
}

@Preview
@Composable
fun AlarmRingScreenPreview(){
    AlarmRingScreen("preview", {})
}