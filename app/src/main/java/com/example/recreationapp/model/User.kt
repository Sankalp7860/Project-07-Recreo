package com.example.recreationapp.model

data class User(
    val uid: String = "",
    val email: String = "",
    val name: String = "",
    val isAdmin: Boolean = false,
    val preferredActivities: List<String> = emptyList() // Add this field
)