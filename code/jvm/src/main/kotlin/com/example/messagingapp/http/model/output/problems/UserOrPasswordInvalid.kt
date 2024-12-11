package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class UserOrPasswordInvalid(
    override val instance: URI
) : Problem(
    type = USER_OR_PASSWORD_INVALID,
    title = "User or password invalid",
    detail = "The user or password is invalid. Please try again.",
    instance = instance,
)
