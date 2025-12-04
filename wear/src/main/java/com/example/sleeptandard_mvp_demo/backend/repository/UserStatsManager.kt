package com.example.sleeptandard_mvp_demo.backend.repository

import android.content.Context
import android.content.SharedPreferences
import kotlin.math.sqrt

class UserStatsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_hr_stats", Context.MODE_PRIVATE)
    
    // Welford 알고리즘 상태
    private var count: Long = 0
    private var mean: Double = 0.0
    private var m2: Double = 0.0

    private val MIN_HR_BPM = 30f
    private val MAX_HR_BPM = 220f

    // [중요] 비트 단위 저장을 위한 키
    private val KEY_COUNT = "KEY_COUNT"
    private val KEY_MEAN_BITS = "KEY_MEAN_BITS"
    private val KEY_M2_BITS = "KEY_M2_BITS"
    
    // 레거시 호환용 키 (마이그레이션 대비)
    private val KEY_MEAN_LEGACY = "KEY_MEAN"

    init {
        loadStats()
    }

    @Synchronized
    fun update(hrValue: Float) {
        // [안정성] 이상치 및 무한대 값 방어
        if (!hrValue.isFinite() || hrValue < MIN_HR_BPM || hrValue > MAX_HR_BPM) return

        count++
        val delta = hrValue - mean
        mean += delta / count
        val delta2 = hrValue - mean
        m2 += delta * delta2
        
        saveStats()
    }

    @Synchronized
    fun getUserMean(): Float = if (count > 0) mean.toFloat() else 65.0f

    @Synchronized
    fun getUserStd(): Float {
        if (count < 2) return 10.0f
        val variance = m2 / (count - 1)
        return sqrt(variance).toFloat()
    }

    private fun saveStats() {
        prefs.edit().apply {
            putLong(KEY_COUNT, count)
            // [정밀도] Double의 비트 패턴을 그대로 저장하여 정밀도 유지
            putLong(KEY_MEAN_BITS, mean.toBits())
            putLong(KEY_M2_BITS, m2.toBits())
            apply()
        }
    }

    private fun loadStats() {
        count = prefs.getLong(KEY_COUNT, 0)
        
        // [호환성] 신규 키가 없으면 레거시(Float) 키에서 복구 시도
        mean = if (prefs.contains(KEY_MEAN_BITS)) {
            Double.fromBits(prefs.getLong(KEY_MEAN_BITS, 0))
        } else {
            prefs.getFloat(KEY_MEAN_LEGACY, 0f).toDouble()
        }

        m2 = if (prefs.contains(KEY_M2_BITS)) {
            Double.fromBits(prefs.getLong(KEY_M2_BITS, 0))
        } else {
            prefs.getFloat("KEY_M2", 0f).toDouble()
        }
    }
}