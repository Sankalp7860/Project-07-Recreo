package com.example.recreationapp.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "", // Added default value
    val isAdmin: Boolean = false // Added default value
)