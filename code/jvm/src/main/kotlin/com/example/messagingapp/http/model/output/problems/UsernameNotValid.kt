package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class UsernameNotValid(
    val username: String,
    override val instance: URI
): Problem(
    type = USERNAME_NOT_VALID,
    title = "Username not valid",
    detail = """
        |The username $username is not valid. 
        |The Username must be between 3 and 64 characters long and contain only letters, numbers, and 
        |underscores.
    """.trimMargin(),
    instance = instance,
)
