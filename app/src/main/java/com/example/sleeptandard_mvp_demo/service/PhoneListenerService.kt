package com.example.sleeptandard_mvp_demo.service

import android.content.Intent
import android.util.Log
import com.example.sleeptandard_mvp_demo.ClassFile.AlarmReceiver
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.ByteBuffer

/**
 * PhoneListenerService - Watch로부터 메시지를 수신하는 서비스
 * 
 * 역할:
 * - /TRIGGER_ALARM: Watch가 감지한 최적의 기상 시점에 알람 트리거
 * - /SLEEP_DATA_RESULT: 수면 데이터 결과 수신 (추후 UI 표시용)
 */
class PhoneListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Message received from Watch: ${messageEvent.path}")
        
        when (messageEvent.path) {
            PATH_TRIGGER_ALARM -> {
                handleTriggerAlarm(messageEvent.data)
            }
            PATH_SLEEP_DATA_RESULT -> {
                handleSleepDataResult(messageEvent.data)
            }
            else -> {
                Log.w(TAG, "Unknown message path: ${messageEvent.path}")
            }
        }
    }
    
    /**
     * /TRIGGER_ALARM 처리
     * Watch가 최적의 기상 시점을 감지했을 때 호출됨
     * 
     * @param data Byte Array containing trigger time (Long, 8 bytes)
     */
    private fun handleTriggerAlarm(data: ByteArray) {
        try {
            // Parse trigger time from Watch
            val triggerTime = if (data.size >= 8) {
                ByteBuffer.wrap(data).long
            } else {
                System.currentTimeMillis()
            }
            
            Log.i(TAG, "Smart Alarm Trigger received! Time: $triggerTime")
            
            // Broadcast to AlarmReceiver to trigger the alarm
            val intent = Intent(this, AlarmReceiver::class.java).apply {
                action = "com.example.sleeptandard_mvp_demo.TRIGGER_ALARM"
                putExtra("label", "Smart Wake-up")
                putExtra("triggerTime", triggerTime)
            }
            
            sendBroadcast(intent)
            Log.d(TAG, "Alarm broadcast sent to AlarmReceiver")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle TRIGGER_ALARM", e)
        }
    }
    
    /**
     * /SLEEP_DATA_RESULT 처리
     * Watch로부터 수면 데이터 결과를 수신
     * 
     * @param data JSON string containing sleep session result
     */
    private fun handleSleepDataResult(data: ByteArray) {
        try {
            val jsonResult = String(data, Charsets.UTF_8)
            Log.i(TAG, "Sleep data result received: ${jsonResult.take(100)}...")
            
            // TODO: Parse JSON and save to local database or display in UI
            // For now, just log it
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle SLEEP_DATA_RESULT", e)
        }
    }
    
    companion object {
        private const val TAG = "PhoneListenerService"
        
        // Message paths from Watch
        private const val PATH_TRIGGER_ALARM = "/TRIGGER_ALARM"
        private const val PATH_SLEEP_DATA_RESULT = "/SLEEP_DATA_RESULT"
    }
}

