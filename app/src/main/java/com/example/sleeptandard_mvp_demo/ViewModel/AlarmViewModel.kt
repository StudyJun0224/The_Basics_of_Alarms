package com.example.sleeptandard_mvp_demo.ViewModel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sleeptandard_mvp_demo.ClassFile.Alarm
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmDay
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.ByteBuffer

class AlarmViewModel(application: Application): AndroidViewModel(application) {
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
    
    // ==================== Watch Communication ====================
    
    /**
     * Watch에게 수면 추적 시작 명령 전송
     * 
     * @param targetTime 목표 알람 시간 (milliseconds)
     */
    fun startSleepTracking(targetTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val messageClient = Wearable.getMessageClient(getApplication())
                val connectedNodes = Tasks.await(Wearable.getNodeClient(getApplication()).connectedNodes)
                
                if (connectedNodes.isEmpty()) {
                    Log.e(TAG, "No connected Watch found")
                    return@launch
                }
                
                // Send to first connected Watch
                val watchNodeId = connectedNodes.first().id
                val payload = ByteBuffer.allocate(8).putLong(targetTime).array()
                
                Tasks.await(messageClient.sendMessage(watchNodeId, PATH_START_TRACKING, payload))
                Log.i(TAG, "START_TRACKING sent to Watch. Target time: $targetTime")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start sleep tracking", e)
            }
        }
    }
    
    /**
     * Watch에게 수면 추적 중지 명령 전송
     */
    fun stopSleepTracking() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val messageClient = Wearable.getMessageClient(getApplication())
                val connectedNodes = Tasks.await(Wearable.getNodeClient(getApplication()).connectedNodes)
                
                if (connectedNodes.isEmpty()) {
                    Log.e(TAG, "No connected Watch found")
                    return@launch
                }
                
                val watchNodeId = connectedNodes.first().id
                
                Tasks.await(messageClient.sendMessage(watchNodeId, PATH_STOP_TRACKING, ByteArray(0)))
                Log.i(TAG, "STOP_TRACKING sent to Watch")
                
            } catch (e: Exception) {
                Log.e(TAG, "Failed to stop sleep tracking", e)
            }
        }
    }
    
    companion object {
        private const val TAG = "AlarmViewModel"
        
        // Message paths to Watch
        private const val PATH_START_TRACKING = "/START_TRACKING"
        private const val PATH_STOP_TRACKING = "/STOP_TRACKING"
    }
}