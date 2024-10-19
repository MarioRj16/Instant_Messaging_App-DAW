package com.example.messagingapp.domain

import kotlinx.datetime.Instant

data class AuthToken(
    val token: Token,
    val userId: Int,
    val createdAt: Instant,
    val lastUsedAt: Instant,
)
