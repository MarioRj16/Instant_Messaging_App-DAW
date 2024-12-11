package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class InternalServerError(
    override val detail: String = "An internal server error occurred. Please try again later.",
    override val instance: URI,
) : Problem(
    type = INTERNAL_SERVER_ERROR,
    title = "Internal server error",
    detail = detail,
    instance = instance,
)
