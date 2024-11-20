package com.example.messagingapp.repository

import com.example.messagingapp.domain.Channel
import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.domain.Membership
import com.example.messagingapp.domain.Message
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

    fun getJoinedChannels(userId: Int): List<Channel>

    fun searchChannels(userId: Int, name: String = ""): List<Channel>

    fun deleteChannel(channelId: Int)

    fun getMessages(channelId: Int): List<Message>

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

    fun listMemberships(channelId: Int): List<Membership>

    fun createChannelInvitation(
        channelId: Int,
        inviterId: Int,
        inviteeId: Int,
        role: String,
        clock: Clock,
    ): Int

    fun listInvitations(userId: Int): List<ChannelInvitation>

    fun getInvitation(invitationId: Int): ChannelInvitation?

    fun getInvitation(channelId: Int, userId: Int): ChannelInvitation?

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
