package com.example.messagingapp.http.model.input

import io.swagger.v3.oas.annotations.media.Schema

data class ChannelSearchInputModel(
    @Schema(description = "The name of the channel to search for", example = "general")
    val channelName: String,
    @Schema(description = "Whether the channel is public or not", example = "true")
    val isPublic: Boolean,
)
