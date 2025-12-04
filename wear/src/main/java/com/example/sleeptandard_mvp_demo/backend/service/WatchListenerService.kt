package com.example.sleeptandard_mvp_demo.backend.service

import android.content.Intent
import android.util.Log
import com.example.sleeptandard_mvp_demo.service.SmartAlarmService
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import java.nio.ByteBuffer

/**
 * WatchListenerService - 폰으로부터 명령을 수신하는 서비스
 * 
 * 역할:
 * - /START_TRACKING: 폰으로부터 targetAlarmTime을 받아 SmartAlarmService 시작
 * - /STOP_TRACKING: SmartAlarmService 중지 및 결과 전송
 */
class WatchListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Message received: ${messageEvent.path}")
        
        when (messageEvent.path) {
            PATH_START_TRACKING -> {
                handleStartTracking(messageEvent.data)
            }
            PATH_STOP_TRACKING -> {
                handleStopTracking()
            }
            else -> {
                Log.w(TAG, "Unknown message path: ${messageEvent.path}")
            }
        }
    }
    
    /**
     * /START_TRACKING 명령 처리
     * 
     * @param data Byte Array containing targetAlarmTime (Long, 8 bytes)
     */
    private fun handleStartTracking(data: ByteArray) {
        try {
            // Parse targetAlarmTime from byte array
            if (data.size < 8) {
                Log.e(TAG, "Invalid data size: ${data.size}, expected 8 bytes")
                return
            }
            
            val targetAlarmTime = ByteBuffer.wrap(data).long
            Log.i(TAG, "START_TRACKING received. Target time: $targetAlarmTime")
            
            // Start SmartAlarmService with targetAlarmTime
            val intent = Intent(this, SmartAlarmService::class.java).apply {
                putExtra(SmartAlarmService.EXTRA_TARGET_TIME, targetAlarmTime)
                action = SmartAlarmService.ACTION_START_TRACKING
            }
            
            startForegroundService(intent)
            Log.d(TAG, "SmartAlarmService started successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle START_TRACKING", e)
        }
    }
    
    /**
     * /STOP_TRACKING 명령 처리
     */
    private fun handleStopTracking() {
        try {
            Log.i(TAG, "STOP_TRACKING received")
            
            // Send stop command to SmartAlarmService
            val intent = Intent(this, SmartAlarmService::class.java).apply {
                action = SmartAlarmService.ACTION_STOP_AND_SEND_RESULT
            }
            
            startService(intent)
            Log.d(TAG, "Stop command sent to SmartAlarmService")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle STOP_TRACKING", e)
        }
    }
    
    companion object {
        private const val TAG = "WatchListenerService"
        
        // Message paths from Phone
        private const val PATH_START_TRACKING = "/START_TRACKING"
        private const val PATH_STOP_TRACKING = "/STOP_TRACKING"
    }
}

