package com.example.messagingapp.domain

enum class InviteRole(
    val role: String,
) {
    MEMBER("member"),
    VIEWER("viewer"),
    ;

    companion object {
        fun fromRole(role: String): InviteRole =
            when (role) {
                "member" -> MEMBER
                "viewer" -> VIEWER
                else -> throw IllegalArgumentException("Invalid role: $role")
            }
    }
}
