package com.example.sleeptandard_mvp_demo.ViewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.sleeptandard_mvp_demo.ClassFile.Alarm
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmDay

class AlarmViewModel: ViewModel() {
    private val _alarms = mutableStateListOf<Alarm>()
    val alarms: List<Alarm> get() = _alarms


    // 알람 추가
    fun addAlarm(hour: Int, minute: Int, isAm: Boolean, days: Set<AlarmDay>, ringtoneUri: String, vibrationEnabled: Boolean): Alarm {
        val newId = if (_alarms.isEmpty()) 1 else _alarms.maxOf { it.id } + 1
        val newAlarm = Alarm(id = newId, hour = hour, minute = minute, isAm = isAm, days = days, ringtoneUri = ringtoneUri, vibrationEnabled = vibrationEnabled)
        _alarms.add(newAlarm)
        return newAlarm
    }

    // 알람 활성화/비활성화
    fun toggleAlarm(id: Int) {
        val index = _alarms.indexOfFirst { it.id == id }
        if (index != -1) {
            val alarm = _alarms[index]
            _alarms[index] = alarm.copy(isOn = !alarm.isOn)
        }
    }

    // 알람 삭제
    fun deleteAlarm(id: Int) {
        _alarms.removeAll { it.id == id }
    }
}