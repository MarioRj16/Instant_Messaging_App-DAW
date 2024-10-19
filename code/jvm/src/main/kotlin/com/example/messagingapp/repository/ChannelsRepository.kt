package com.example.messagingapp.repository

import com.example.messagingapp.http.model.ChannelInvitationOutput
import com.example.messagingapp.http.model.ChannelWithMembership
import com.example.messagingapp.http.model.MembershipOutput
import com.example.messagingapp.http.model.MessageOutput

interface ChannelsRepository {
    fun createChannel(
        channelName: String,
        isPublic: Boolean,
        ownerId: Int,
    ): Int

    fun getChannel(
        channelId: Long,
        userId: Int,
    ): ChannelWithMembership?

    fun getJoinedChannels(userId: Int): List<ChannelWithMembership>

    fun searchChannels(): List<ChannelWithMembership>

    fun joinChannel(
        channelId: Long,
        userId: Int,
    ): Unit

    fun getMessages(channelId: Long): List<MessageOutput>

    fun sendMessage(
        channelId: Long,
        userId: Int,
        content: String,
    ): Int

    fun getMembership(
        channelId: Long,
        userId: Int,
    ): MembershipOutput?

    fun getMemberships(channelId: Long): List<MembershipOutput>

    fun inviteMember(
        channelId: Long,
        userId: Int,
        invitedUserId: Int,
        role: String,
        expiresAt: Long,
    ): Int

    fun getInvitations(userId: Int): List<ChannelInvitationOutput>

    fun getInvitation(
        inviterId: Int,
        inviteeId: Int,
        channelId: Int,
    ): ChannelInvitationOutput?

    fun getInvitationById(invitationId: Long): ChannelInvitationOutput?

    // fun getPendingInvitationById(invitationId: Long): ChannelInvitationOutput?

    fun acceptInvitation(
        invitationId: Long,
        userId: Int,
        channelId: Int,
        role: String,
    ): Int

    fun declineInvitation(invitationId: Long): Int

    fun leaveChannel(
        channelId: Long,
        userId: Int,
    )

    // fun kickMembers(channelId: Long,usersId:List<Int>): Boolean?
}
