package com.example.recreationapp.model

data class User(
    val uid: String,
    val email: String,
    val name: String, // Added name field
    val isAdmin: Boolean = false
)