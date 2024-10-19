package com.example.messagingapp.domain

import java.time.LocalDateTime

data class Channel(
    val channelId: Int,
    val channelName: String,
    val owner: User,
    val createdAt: LocalDateTime,
    val isPublic: Boolean,
    val members: Set<User>,
    val viewers: Set<User>,
) {
    /*  Both members and viewers are made to be sets to make search O(1)
    and because the order here is not relevant.*/
}
