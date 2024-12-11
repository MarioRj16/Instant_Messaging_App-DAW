package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class InvalidChannelName(
    val channelName: String,
    override val instance: URI,
) : Problem(
    type = INVALID_CHANNEL_NAME,
    title = "Invalid channel name",
    detail = """
        |The channel name '$channelName' is invalid.
        |The channel name must be between 3 and 20 characters long and contain only letters, numbers, and underscores.""".trimMargin(),
    instance = instance,
)
