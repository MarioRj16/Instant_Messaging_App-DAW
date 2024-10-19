package com.example.messagingapp.domain

enum class InviteStatus(
    val status: String,
) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    ;

    companion object {
        fun fromValue(status: String): InviteStatus =
            when (status) {
                "pending" -> PENDING
                "accepted" -> ACCEPTED
                "rejected" -> REJECTED
                else -> throw IllegalArgumentException("Unknown value: $status")
            }
    }
}
