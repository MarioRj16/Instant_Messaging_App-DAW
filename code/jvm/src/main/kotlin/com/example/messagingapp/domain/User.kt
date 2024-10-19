package com.example.messagingapp.domain

data class User(
    val userId: Int,
    val username: String,
    val email: String,
    val password: Password,
)
