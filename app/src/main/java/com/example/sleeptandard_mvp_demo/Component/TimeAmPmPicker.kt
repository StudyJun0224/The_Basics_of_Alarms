package com.example.sleeptandard_mvp_demo.Component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.chargemap.compose.numberpicker.AMPMHours
import com.chargemap.compose.numberpicker.Hours
import com.chargemap.compose.numberpicker.HoursNumberPicker

@Composable
fun TimeAmPmPicker (
    defaultHour12: Int = 8,
    defaultMinute: Int = 30,
    defaultDay: AMPMHours.DayTime = AMPMHours.DayTime.AM,
    onTimeChange: (hour12: Int, minute: Int, isAm: Boolean) -> Unit
){
    var pickerValue = remember {
        mutableStateOf<Hours>(
            AMPMHours(
                defaultHour12,
                defaultMinute,
                defaultDay
            )
        )
    }
    HoursNumberPicker(
        dividersColor = MaterialTheme.colorScheme.primary,
        value = pickerValue.value,
        onValueChange = {
            pickerValue.value = it

            val ampm = it as? AMPMHours ?: return@HoursNumberPicker
            onTimeChange(
                ampm.hours,
                ampm.minutes,
                ampm.dayTime == AMPMHours.DayTime.AM
            )
        },
        hoursDivider = {
            Text(
                modifier = Modifier
                    .width(16.dp)
                    .padding(horizontal = 4.dp),
                text = ":",
                textAlign = TextAlign.Center
            )
        },
        minutesDivider = {
            Spacer(modifier = Modifier.width(8.dp))
        }
    )

}