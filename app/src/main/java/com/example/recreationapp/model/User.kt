package com.example.recreationapp.model

data class User(
    val uid: String = "", // Firebase UID
    val email: String = "",
    val isAdmin: Boolean = false
)