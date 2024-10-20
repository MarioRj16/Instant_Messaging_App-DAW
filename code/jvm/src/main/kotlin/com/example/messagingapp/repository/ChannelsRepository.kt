package com.example.messagingapp.repository

import com.example.messagingapp.http.model.output.ChannelInvitationOutputModel
import com.example.messagingapp.http.model.output.ChannelWithMembershipOutputModel
import com.example.messagingapp.http.model.output.MembershipOutputModel
import com.example.messagingapp.http.model.output.MessageOutputModel

interface ChannelsRepository {
    fun createChannel(
        channelName: String,
        isPublic: Boolean,
        ownerId: Int,
    ): Int

    fun getChannel(
        channelId: Long,
        userId: Int,
    ): ChannelWithMembershipOutputModel?

    fun getJoinedChannels(userId: Int): List<ChannelWithMembershipOutputModel>

    fun searchChannels(): List<ChannelWithMembershipOutputModel>

    fun joinChannel(
        channelId: Long,
        userId: Int,
    ): Unit

    fun getMessages(channelId: Long): List<MessageOutputModel>

    fun sendMessage(
        channelId: Long,
        userId: Int,
        content: String,
    ): Int

    fun getMembership(
        channelId: Long,
        userId: Int,
    ): MembershipOutputModel?

    fun getMemberships(channelId: Long): List<MembershipOutputModel>

    fun inviteMember(
        channelId: Long,
        userId: Int,
        invitedUserId: Int,
        role: String,
        expiresAt: Long,
    ): Int

    fun getInvitations(userId: Int): List<ChannelInvitationOutputModel>

    fun getInvitation(
        inviterId: Int,
        inviteeId: Int,
        channelId: Int,
    ): ChannelInvitationOutputModel?

    fun getInvitationById(invitationId: Long): ChannelInvitationOutputModel?

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
