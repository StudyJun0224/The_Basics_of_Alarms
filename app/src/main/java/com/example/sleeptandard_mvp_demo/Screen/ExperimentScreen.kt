package com.example.sleeptandard_mvp_demo.Screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.sleeptandard_mvp_demo.Component.CustomTimePicker

@Composable
fun ExperimentScreen(){

    var h by remember{ mutableIntStateOf(8)}
    var m by remember { mutableIntStateOf(30) }
    var ampm by remember{mutableStateOf(true)}
    var isAm by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text("It is Experiment Screen")


        CustomTimePicker(
            onTimeChange = {hour12, minute, isAm ->
               h = hour12
               m = minute
               ampm = isAm
            }
        )

        if(ampm)  isAm = "am" else isAm = "pm"


        Text("${isAm} hour: ${h} minute: ${m} ")

    }

}