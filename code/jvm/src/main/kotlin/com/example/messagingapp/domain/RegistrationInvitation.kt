package com.example.messagingapp.domain

import kotlinx.datetime.Instant
import java.util.UUID

data class RegistrationInvitation(
    val invitationToken: UUID,
    val inviterId: Int,
    val createdAt: Instant,
    val invitationStatus: InviteStatus,
)
