package com.example.sleeptandard_mvp_demo.Component

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.FlingBehavior
import androidx.compose.foundation.gestures.ScrollScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WheelPicker(
    modifier: Modifier = Modifier,
    items: List<String>,
    visibleCount: Int = 3,
    itemHeight: Dp = 62.dp,

    // 🔥 순환/리센터 옵션
    isCyclic: Boolean = false,
    cycles: Int = 200,          // 가짜 반복 횟수 (충분히 크게)

    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = 45.sp
    ),
    fadedTextStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        fontSize = 43.sp
    ),
) {
    require(visibleCount % 2 == 1)

    val baseSize = items.size
    val centerOffset = visibleCount / 2
    val virtualCount = if (isCyclic) baseSize * cycles else baseSize

    val state = rememberLazyListState()

    // gpt가 지적한 초기 컴포지션 오류를 위한 억제기 생성
    var didInitialPosition by remember { mutableStateOf(false) }
    var suppressCallback by remember { mutableStateOf(false) }


    val snapFling = rememberSnapFlingBehavior(lazyListState = state)

    // 스크롤 속도조절 값
    val slowFling = remember(snapFling) {
        VelocityScalingFlingBehavior(
            base = snapFling,
            velocityFactor = 0.5f
        )
    }

    /* -----------------------------
     * 1️⃣ 현재 "중앙에 보이는 가상 인덱스"
     * ----------------------------- */
    val centeredVirtualIndex by remember {
        derivedStateOf {
            val layout = state.layoutInfo
            if (layout.visibleItemsInfo.isEmpty()) return@derivedStateOf 0

            val viewportCenter =
                (layout.viewportStartOffset + layout.viewportEndOffset) / 2

            layout.visibleItemsInfo.minByOrNull { info ->
                val itemCenter = info.offset + info.size / 2
                abs(itemCenter - viewportCenter)
            }?.index ?: 0
        }
    }

    /* -----------------------------
     * 2️⃣ 가상 인덱스 → 실제 인덱스(0..59)
     * ----------------------------- */
    val centeredRealIndex by remember {
        derivedStateOf {
            if (baseSize == 0) 0
            else ((centeredVirtualIndex % baseSize) + baseSize) % baseSize
        }
    }

    /* -----------------------------
     * 3️⃣ 초기 진입 / 외부 값 변경 시
     *    → "가운데"로 이동
     * ----------------------------- */
    LaunchedEffect(selectedIndex, isCyclic) {
        if (baseSize == 0) return@LaunchedEffect

        suppressCallback = true

        if (isCyclic) {
            val middle = (virtualCount / 2) - ((virtualCount / 2) % baseSize)
            state.scrollToItem(middle + selectedIndex)
        } else {
            state.scrollToItem(selectedIndex.coerceIn(0, baseSize - 1))
        }

        didInitialPosition = true
        suppressCallback = false
    }

    /* -----------------------------
     * 4️⃣ 스크롤 멈추면 선택 확정 + 리센터
     * ----------------------------- */
    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {

            // ✅ 초기 위치 잡기 전엔 스킵
            if (!didInitialPosition) return@LaunchedEffect

            // ✅ 프로그램 스크롤(초기/외부 selectedIndex 반영) 중엔 스킵
            if (suppressCallback) return@LaunchedEffect

            onSelectedIndexChange(centeredRealIndex)

            if (isCyclic) {
                val threshold = baseSize * 2
                val min = threshold
                val max = virtualCount - threshold

                if (centeredVirtualIndex < min || centeredVirtualIndex > max) {
                    val middle = (virtualCount / 2) - ((virtualCount / 2) % baseSize)

                    // 리센터도 프로그램 스크롤이므로 콜백 억제
                    suppressCallback = true
                    state.scrollToItem(middle + centeredRealIndex)
                    suppressCallback = false
                }
            }
        }
    }

    Box(
        modifier = modifier
            .height(itemHeight * visibleCount)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
            flingBehavior = slowFling,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight * centerOffset)
        ) {
            items(virtualCount) { virtualIndex ->
                val realIndex =
                    if (baseSize == 0) 0
                    else ((virtualIndex % baseSize) + baseSize) % baseSize

                val distance = abs(virtualIndex - centeredVirtualIndex)

                val alpha = when (distance) {
                    0 -> 1f
                    1 -> 0.35f
                    else -> 0.15f
                }
                val scale = if (distance == 0) 1.15f else 1f

                Box(
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = items[realIndex],
                        style = if (distance == 0) textStyle else fadedTextStyle,
                        modifier = Modifier.graphicsLayer {
                            this.alpha = alpha
                            scaleX = scale
                            scaleY = scale
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomTimePicker(
    modifier: Modifier = Modifier,
    defaultHour12: Int = 6,
    defaultMinute: Int = 0,
    defaultIsAm: Boolean = true,
    onTimeChange: (hour12: Int, minute: Int, isAm: Boolean) -> Unit
) {
    val ampmItems = listOf("AM", "PM")
    val hourItems = (1..12).map { it.toString() }
    val minuteItems = (0..59).map { it.toString().padStart(2, '0') }

    var ampmIndex by remember { mutableIntStateOf(if (defaultIsAm) 0 else 1) }
    var hourIndex by remember { mutableIntStateOf((defaultHour12 - 1).coerceIn(0, 11)) }
    var minuteIndex by remember { mutableIntStateOf(defaultMinute.coerceIn(0, 59)) }


    // 값 바뀔 때마다 콜백
    LaunchedEffect(ampmIndex, hourIndex, minuteIndex) {
        onTimeChange(hourIndex + 1, minuteIndex, ampmIndex == 0)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        WheelPicker(
            modifier = Modifier.width(90.dp),
            items = ampmItems,
            itemHeight = 42.dp,
            selectedIndex = ampmIndex,
            onSelectedIndexChange = { ampmIndex = it },
            textStyle = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 27.sp
            ),
            fadedTextStyle = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
                fontSize = 25.sp
            ),
        )

        Spacer(Modifier.width(12.dp))

        WheelPicker(
            modifier = Modifier.width(90.dp),
            items = hourItems,
            selectedIndex = hourIndex,
            onSelectedIndexChange = { hourIndex = it },
            isCyclic = true,

        )

        Text(
            text = ":",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 45.sp
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        WheelPicker(
            modifier = Modifier.width(90.dp),
            items = minuteItems,
            selectedIndex = minuteIndex,
            onSelectedIndexChange = { minuteIndex = it },
            isCyclic = true,
        )
    }
}

private class VelocityScalingFlingBehavior(
    private val base: FlingBehavior,
    private val velocityFactor: Float
) : FlingBehavior {
    override suspend fun ScrollScope.performFling(initialVelocity: Float): Float {
        return with(base) { performFling(initialVelocity * velocityFactor) }
    }
}

@Preview
@Composable
fun CustomTimePickerPreview(){
    var h = 3
    var m = 30
    var ampm = true

    CustomTimePicker(onTimeChange = {hour12, minute, isAm ->
        h = hour12
        m = minute
        ampm = isAm
    })
}
