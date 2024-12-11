package com.example.messagingapp.http.model.output.problems

import com.example.messagingapp.http.model.output.Problem
import java.net.URI

data class InvalidPagination(override val instance: URI): Problem(
    INVALID_PAGINATION,
    "Invalid pagination",
    "The pagination parameters are invalid. Both must be positive integers.",
    instance,
)
