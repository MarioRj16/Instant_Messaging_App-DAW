package com.example.messagingapp.http.model

data class ChannelWithMembership(
    val channelId: Int,
    val channelName: String,
    val isPublic: Boolean,
    val ownerId: Int,
    val isMember: Boolean,
    val createdAt: String,
)
