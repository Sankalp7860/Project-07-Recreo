package com.example.recreationapp.model

data class ActivityRecord(
    val id: String = "", // Realtime Database key
    val userId: String = "",
    val activityType: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)