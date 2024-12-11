package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class UserNotAuthorizedToWrite(
    override val instance: URI,
): Problem(
    type = USER_NOT_AUTHORIZED_TO_WRITE,
    title = "User not authorized to write",
    detail = "The user is not authorized to write to the channel.",
    instance = instance,
)
