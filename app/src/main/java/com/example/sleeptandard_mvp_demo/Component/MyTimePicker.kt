package com.example.sleeptandard_mvp_demo.Component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    textStyle: TextStyle = MaterialTheme.typography.bodyLarge.copy(
        fontSize = 45.sp
    ),
    fadedTextStyle: TextStyle = MaterialTheme.typography.displaySmall.copy(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f),
        fontSize = 43.sp
    ),
) {
    require(visibleCount % 2 == 1)
    val centerOffset = visibleCount / 2

    val state = rememberLazyListState()

    // ✅ 스냅을 "센터" 기준으로
    val fling = rememberSnapFlingBehavior(
        lazyListState = state,
        snapPosition = SnapPosition.Center
    )

    // ✅ 뷰포트 중앙에 가장 가까운 아이템 인덱스 계산
    val centeredIndex by remember {
        derivedStateOf {
            val layout = state.layoutInfo
            val viewportCenter = (layout.viewportStartOffset + layout.viewportEndOffset) / 2

            val closest = layout.visibleItemsInfo.minByOrNull { info ->
                val itemCenter = info.offset + info.size / 2
                abs(itemCenter - viewportCenter)
            }

            closest?.index ?: selectedIndex
        }
    }

    // ✅ 외부 selectedIndex 바뀌면 해당 아이템을 중앙으로 오게 스크롤
    LaunchedEffect(selectedIndex) {
        // contentPadding 때문에 scrollToItem만 해도 중앙에 오기 쉬움
        state.scrollToItem(selectedIndex)
    }

    // ✅ 스크롤이 멈추면 중앙 아이템을 선택값으로 확정
    LaunchedEffect(state.isScrollInProgress) {
        if (!state.isScrollInProgress) {
            onSelectedIndexChange(centeredIndex.coerceIn(0, items.lastIndex))
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
            flingBehavior = fling,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight * centerOffset)
        ) {
            items(items.size) { index ->
                val distance = abs(index - centeredIndex)

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
                        text = items[index],
                        style = if (distance == 0) textStyle else fadedTextStyle,
                        modifier = Modifier.graphicsLayer {
                            this.alpha = alpha
                            scaleX = scale
                            scaleY = scale
                        },

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
            fadedTextStyle = MaterialTheme.typography.displaySmall.copy(
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

        )

        Text(
            text = ":",
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        WheelPicker(
            modifier = Modifier.width(90.dp),
            items = minuteItems,
            selectedIndex = minuteIndex,
            onSelectedIndexChange = { minuteIndex = it },
        )
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
