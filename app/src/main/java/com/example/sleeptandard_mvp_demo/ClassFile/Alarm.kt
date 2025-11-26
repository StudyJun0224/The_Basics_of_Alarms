package com.example.sleeptandard_mvp_demo.ClassFile

enum class AlarmDay(val label: String) {
    MON("월"),
    TUE("화"),
    WED("수"),
    THU("목"),
    FRI("금"),
    SAT("토"),
    SUN("일")
}

data class Alarm (
    val id: Int,
    val hour: Int,
    val minute: Int,
    val isAm: Boolean,
    val days: Set<AlarmDay> = emptySet(),
    val ringtoneUri: String = "",
    val vibrationEnabled: Boolean = true,
    val isOn: Boolean = true
    )