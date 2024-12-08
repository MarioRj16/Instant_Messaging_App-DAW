package com.example.messagingapp.http.model.output

import io.swagger.v3.oas.annotations.media.Schema

data class ChannelCreateOutputModel(
    @Schema(description = "The ID of the created channel", example = "1")
    val channelId: Int,
)
