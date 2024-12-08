package com.example.messagingapp.http.model.input

import io.swagger.v3.oas.annotations.media.Schema

data class MessageInputModel(
    @Schema(description = "The content of the message", example = "Hello, World!")
    val content: String
)
