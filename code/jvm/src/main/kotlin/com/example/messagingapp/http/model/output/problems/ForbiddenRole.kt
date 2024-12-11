package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class ForbiddenRole(
    override val instance: URI
): Problem(
    type = FORBIDDEN_ROLE,
    title = "Forbidden role",
    detail = "The role is forbidden.",
    instance = instance,
)
