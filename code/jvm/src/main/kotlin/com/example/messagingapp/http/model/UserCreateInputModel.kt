package com.example.messagingapp.http.model

data class UserCreateInputModel(
    val username: String,
    val password: String,
    val invitationToken: String,
    val email: String,
)
