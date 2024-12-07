package com.example.messagingapp.http.model.input

data class UserCreateInputModel(
    val username: String,
    val password: String,
    val invitationCode: String,
)
