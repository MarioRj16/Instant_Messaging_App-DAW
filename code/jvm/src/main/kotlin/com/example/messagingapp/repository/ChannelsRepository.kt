package com.example.messagingapp.repository

import com.example.messagingapp.DEFAULT_PAGE
import com.example.messagingapp.DEFAULT_PAGE_SIZE
import com.example.messagingapp.domain.Channel
import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.domain.Membership
import com.example.messagingapp.domain.Message
import com.example.messagingapp.utils.PaginatedResponse
import kotlinx.datetime.Clock

interface ChannelsRepository {
    fun createChannel(
        channelName: String,
        isPublic: Boolean,
        ownerId: Int,
        clock: Clock,
    ): Int

    fun getChannel(
        channelId: Int,
        userId: Int,
    ): Channel?

    fun listJoinedChannels(
        userId: Int,
        page: Int = DEFAULT_PAGE.toInt(),
        pageSize: Int = DEFAULT_PAGE_SIZE.toInt(),
    ): PaginatedResponse<Channel>

    fun searchChannels(
        userId: Int,
        name: String = "",
        page: Int = DEFAULT_PAGE.toInt(),
        pageSize: Int = DEFAULT_PAGE_SIZE.toInt(),
    ): PaginatedResponse<Channel>

    fun deleteChannel(channelId: Int)

    fun listMessages(channelId: Int): List<Message>

    fun createMessage(
        channelId: Int,
        userId: Int,
        content: String,
        clock: Clock,
    ): Int

    fun getMembership(
        channelId: Int,
        userId: Int,
    ): Membership?

    fun listMemberships(
        channelId: Int,
        page: Int = DEFAULT_PAGE.toInt(),
        pageSize: Int = DEFAULT_PAGE_SIZE.toInt(),
    ): PaginatedResponse<Membership>

    fun createChannelInvitation(
        channelId: Int,
        inviterId: Int,
        inviteeId: Int,
        role: String,
        clock: Clock,
    ): Int

    fun listInvitations(
        userId: Int,
        page: Int = DEFAULT_PAGE.toInt(),
        pageSize: Int = DEFAULT_PAGE_SIZE.toInt(),
    ): PaginatedResponse<ChannelInvitation>

    fun getInvitation(invitationId: Int): ChannelInvitation?

    fun getInvitation(
        channelId: Int,
        userId: Int,
    ): ChannelInvitation?

    fun deleteInvitation(invitationId: Int)

    fun deleteMembership(
        channelId: Int,
        userId: Int,
    )

    fun createMembership(
        userId: Int,
        channelId: Int,
        clock: Clock,
        role: String,
    )
}
