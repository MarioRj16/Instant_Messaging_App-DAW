package com.example.messagingapp.http.model.input

import io.swagger.v3.oas.annotations.media.Schema

data class LoginInputModel(
    @Schema(description = "Username", example = "user")
    val username: String,
    @Schema(description = "Password", example = "password")
    val password: String,
)
