package com.example.messagingapp.http.model.input

import io.swagger.v3.oas.annotations.media.Schema

data class UserCreateInputModel(
    @Schema(description = "Username", example = "user123")
    val username: String,
    @Schema(description = "Password", example = "password123")
    val password: String,
    @Schema(description = "Invitation Code", example = "KV3A")
    val invitationCode: String,
)
