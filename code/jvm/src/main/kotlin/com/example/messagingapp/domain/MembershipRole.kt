package com.example.messagingapp.domain

enum class MembershipRole(
    val role: String,
    val value: Int,
) {
    OWNER("owner", 3),
    MEMBER("member", 2),
    VIEWER("viewer", 1),
    ;

    companion object {
        fun fromRole(role: String): MembershipRole =
            when (role) {
                "owner" -> OWNER
                "member" -> MEMBER
                "viewer" -> VIEWER
                else -> throw IllegalArgumentException("Invalid role: $role")
            }
    }
}
