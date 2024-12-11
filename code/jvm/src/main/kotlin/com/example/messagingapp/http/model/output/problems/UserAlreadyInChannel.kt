package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class UserAlreadyInChannel(
    override val instance: URI,
) : Problem(
    type = USER_ALREADY_IN_CHANNEL,
    title = "User already in channel",
    detail = "The user is already in the channel.",
    instance = instance,
)
