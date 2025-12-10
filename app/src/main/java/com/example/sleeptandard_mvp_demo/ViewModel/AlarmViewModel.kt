package com.example.sleeptandard_mvp_demo.ViewModel

import android.media.Ringtone
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.sleeptandard_mvp_demo.ClassFile.Alarm

class AlarmViewModel: ViewModel() {

    private var _alarm by mutableStateOf(Alarm())
    val alarm: Alarm get() = _alarm
    private val _alarms = mutableStateListOf<Alarm>()
    val alarms: List<Alarm> get() = _alarms

    fun saveAlarm(hour: Int, minute: Int, isAm: Boolean, ringtoneUri: String, vibrationEnabled: Boolean): Boolean {

        _alarm = Alarm(1, hour, minute, isAm, ringtoneUri, vibrationEnabled)
        return true
    }

    // 외부에서 Alarm 객체를 통째로 넣어줄 수 있게
    fun copyAlarm(alarm: Alarm) {
        _alarm = alarm
    }

    // 알람 추가
    fun addAlarm(hour: Int, minute: Int, isAm: Boolean,  ringtoneUri: String, vibrationEnabled: Boolean): Alarm {
        val newId = if (_alarms.isEmpty()) 1 else _alarms.maxOf { it.id } + 1
        val newAlarm = Alarm(id = newId, hour = hour, minute = minute, isAm = isAm,  ringtoneUri = ringtoneUri, vibrationEnabled = vibrationEnabled)
        _alarms.add(newAlarm)
        return newAlarm
    }
    /* Not using : 알람 설정완료 여부로 판단
    // 알람 활성화/비활성화
    fun toggleAlarm(id: Int) {
        val index = _alarms.indexOfFirst { it.id == id }
        if (index != -1) {
            val alarm = _alarms[index]
            _alarms[index] = alarm.copy(isOn = !alarm.isOn)
        }
    }*/

    // 알람 삭제
    fun deleteAlarm(id: Int) {
        _alarms.removeAll { it.id == id }
    }

}