package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class ChannelNotFound(
    override val instance: URI,
) : Problem(
    type = CHANNEL_NOT_FOUND,
    title = "Channel not found",
    detail = "The channel was not found.",
    instance = instance,
)
