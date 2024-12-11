package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class PasswordNotSafe(
    override val instance: URI,
) : Problem(
    type = PASSWORD_NOT_SAFE,
    title = "Password is not safe",
    detail = """
            The password must be at least 8 characters long 
            and contain at least one uppercase letter, 
            one lowercase letter, 
            one number, 
            and one special character.
            """.trimIndent(),
    instance = instance,
)
