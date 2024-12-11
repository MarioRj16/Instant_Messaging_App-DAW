package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class ChannelNotPublic(
    override val instance: URI
): Problem(
    type = CHANNEL_NOT_PUBLIC,
    title = "Channel not public",
    detail = "The channel is not public.",
    instance = instance,
)
