package com.example.sleeptandard_mvp_demo.Screen

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.sleeptandard_mvp_demo.ClassFile.Alarm
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmScheduler
import com.example.sleeptandard_mvp_demo.Component.AlarmBottomNavBar
import com.example.sleeptandard_mvp_demo.Prefs.AlarmPreferences
import com.example.sleeptandard_mvp_demo.ViewModel.AlarmViewModel
import com.example.sleeptandard_mvp_demo.ui.theme.AppIcons
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun SettedAlarmScreen(
    alarmViewModel: AlarmViewModel,
    scheduler: AlarmScheduler,
    onTurnAlarmOff : ()-> Unit
) {
    val context = LocalContext.current   // ✨ 추가

    val alarm: Alarm = alarmViewModel.alarm

    var selectedIndex by remember { mutableStateOf(0) }

    // ✅ 타이밍/거리 조절 값 (취향대로 조절)
    val stayMs = 1200L
    val moveMs = 320
    val shift = 18.dp

// 1번이 위로 올라가기 시작하는 타이밍 = 1번이 중앙에 머무는 시간
    val liftStartDelay = stayMs
// 2번 텍스트가 등장하는 타이밍 = 1번 이동이 끝난 다음
    val textStartDelay = stayMs + moveMs.toLong()

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LiftUpOnce(
            startDelayMs = liftStartDelay,
            moveMs = moveMs,
            lift = shift
        ) { liftedModifier ->
            Column(
                modifier = liftedModifier.padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(283.dp))

                Surface(
                    modifier = Modifier.height(52.dp).width(196.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text(
                            getWakeUpTimeRange(alarm.hour, alarm.minute),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "사이에 깨워드립니다",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.5.dp))

        // ✅ 2번/3번: 1번이 위로 이동 끝난 다음에 나타나고, 텍스트만 위로 밀리며 3번까지 나오면 멈춤
        DelayedContent(delayMs = textStartDelay) {
            StackedRollingText(
                texts = listOf("데이터 센싱 시작", "워치를 착용해주세요"),
                modifier = Modifier.fillMaxWidth(),
                stayMs = stayMs,
                moveMs = moveMs,
                shift = shift,
                maxLines = 2
            )
        }

        Spacer(modifier = Modifier.height(33.dp))

        Image(
            modifier = Modifier
                .width(50.dp)
                .height(24.dp),
            painter = painterResource(AppIcons.SettedActivity),
            contentDescription = "찌릿찌릿",
            contentScale = ContentScale.FillBounds
        )

        Spacer(modifier = Modifier.height(135.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth(193f / 350f)
                .height(67.dp),
            onClick = {
                onTurnAlarmOff()
                scheduler.cancel(alarmViewModel.alarm)

                // 2) SharedPreferences 플래그/값 삭제
                val alarmPrefs = AlarmPreferences(context)
                alarmPrefs.clearAlarm()

                // 3) 네비게이션 처리
                onTurnAlarmOff()
            }
        ) { Text("알람중지") }
    }
}



fun getWakeUpTimeRange(hour:Int, minute: Int): String{
    var earlyTotalMinute: Int = (hour * 60 + minute) - 30

    if(earlyTotalMinute < 0) earlyTotalMinute += 12 * 60

    return String.format(
        Locale.getDefault(),
        "%d : %02d ~ %d : %02d",
        earlyTotalMinute/60, earlyTotalMinute%60, hour, minute)
}

@Preview
@Composable
fun Precure(){
    var selectedIndex by remember { mutableStateOf(0) }

    Scaffold(
        bottomBar = {
            AlarmBottomNavBar(
                selectedIndex = selectedIndex,
                onSelect = { selectedIndex = it }
            )
        }
    ){
        innerpadding ->

        Column(
            modifier = Modifier
                .padding(innerpadding)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            // 1번 아이템
            Column(
                modifier = Modifier
                    .padding(10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(modifier = Modifier.height(283.dp))


                Surface(
                    modifier = Modifier
                        .height(52.dp)
                        .width(196.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,

                    ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("5 : 30 ~ 6 : 00",
                            style = MaterialTheme.typography.bodyLarge)
                    }
                }

                Spacer(
                    modifier = Modifier
                        .height(12.dp)
                )

                Text("사이에 깨워드립니다",
                    style = MaterialTheme.typography.bodyMedium)
            }

            Spacer(modifier = Modifier.height(32.5f.dp))

            // 2번 아이템
            Text("데이터 센싱 시작",
                style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(9.dp))

            // 3번 아이템
            Text("워치를 착용해주세요",
                style = MaterialTheme.typography.bodyLarge)

            Spacer(modifier = Modifier.height(33.dp))

            // 2번 아이템 (얘는 위치 고정)
            Image(
                modifier = Modifier
                    .width(50.dp)
                    .height(24.dp),
                painter = painterResource(AppIcons.SettedActivity),
                contentDescription = "찌릿찌릿",
                contentScale = ContentScale.FillBounds
            )

            Spacer(modifier = Modifier.height(135.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth(193f / 350f)
                    .height(67.dp),
                onClick = {}
            ) { Text("알람중지") }
        }
    }
}

@Composable
private fun LiftUpOnce(
    startDelayMs: Long,
    moveMs: Int,
    lift: Dp,
    content: @Composable (Modifier) -> Unit
) {
    val anim = remember { Animatable(0f) } // 0 -> 1

    val liftPx = with(LocalDensity.current) { lift.toPx() }

    LaunchedEffect(Unit) {
        delay(startDelayMs)
        anim.animateTo(
            1f,
            animationSpec = tween(durationMillis = moveMs, easing = FastOutSlowInEasing)
        )
    }

    val modifier = Modifier.graphicsLayer {
        translationY = -liftPx * anim.value
    }

    content(modifier)
}

@Composable
private fun DelayedContent(
    delayMs: Long,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMs)
        visible = true
    }
    if (visible) content()
}
