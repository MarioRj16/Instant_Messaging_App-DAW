package com.example.messagingapp.services

import com.example.messagingapp.domain.ChannelDomain
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.repository.TransactionManager
import com.example.messagingapp.utils.failure
import com.example.messagingapp.utils.success
import kotlinx.datetime.Clock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class ChannelService(
    private val transactionManager: TransactionManager,
    private val channelDomain: ChannelDomain,
    private val clock: Clock,
) {
    fun createChannel(
        channelName: String,
        userId: Int,
        isPublic: Boolean,
    ): ChannelCreationResult =
        transactionManager.run {
            if (!channelDomain.isValidName(channelName)) {
                return@run failure(ChannelCreationError.NameIsNotValid)
            }

            if(
                it.channelsRepository.searchChannels(userId, channelName)
                    .any { channel -> channel.channelName == channelName }
            ) {
                return@run failure(ChannelCreationError.NameAlreadyExists)
            }

            val channelId = it.channelsRepository.createChannel(channelName, isPublic, userId, clock)
            it.channelsRepository.createMembership(userId, channelId, clock, MembershipRole.OWNER.role)
            success(channelId)
        }

    fun getChannel(
        channelId: Int,
        userId: Int,
    ): ChannelGetResult =
        transactionManager.run {
            val channel =
                it.channelsRepository.getChannel(channelId, userId)
                    ?: return@run failure(ChannelGetError.ChannelDoesNotExist)

            success(channel)
        }

    fun getJoinedChannels(userId: Int): GetJoinedChannelsResult =
        transactionManager.run {
            val channels = it.channelsRepository.getJoinedChannels(userId)
            success(channels)
        }
    
    fun searchChannels(userId: Int, channelName: String): SearchChannelsResult =
        transactionManager.run {
            val channels = it.channelsRepository.searchChannels(userId, channelName)
            success(channels)
        }

    fun joinPublicChannel(
        channelId: Int,
        userId: Int,
    ): JoinChannelResult {
        return transactionManager.run {
            val channel =
                it.channelsRepository.getChannel(channelId, userId)
                    ?: return@run failure(JoinChannelError.ChannelDoesNotExist)

            if (!channel.isPublic) {
                return@run failure(JoinChannelError.ChannelIsNotPublic)
            }

            if (it.channelsRepository.getMembership(channelId, userId) != null) {
                return@run failure(JoinChannelError.UserIsAlreadyMember)
            }

            success(it.channelsRepository.createMembership(userId, channelId, clock, MembershipRole.MEMBER.role))
        }
    }

    fun acceptChannelInvitation(channelId: Int, userId: Int): AcceptChannelInvitationResult =
        transactionManager.run {
            val invitation = it.channelsRepository.getInvitation(channelId, userId)
                ?: return@run failure(AcceptChannelInvitationError.InvitationNotFound)

            // We'll delete the invitation even if the user is already a member
            it.channelsRepository.deleteInvitation(invitation.channelInvitationId)

            val membership = it.channelsRepository.getMembership(channelId, userId)
            if (membership != null) {
                return@run failure(AcceptChannelInvitationError.UserIsAlreadyMember)
            }

            it.channelsRepository.createMembership(userId, channelId, clock, invitation.role.role)
            success(Unit)
        }

    fun declineChannelInvitation(channelId: Int, userId: Int): DeclineChannelInvitationResult {
        return transactionManager.run {
            val invitation = it.channelsRepository.getInvitation(channelId, userId)
                ?: return@run failure(DeclineChannelInvitationError.InvitationNotFound)

            // We'll delete the invitation even if the user is already a member
            it.channelsRepository.deleteInvitation(invitation.channelInvitationId)

            val membership = it.channelsRepository.getMembership(channelId, userId)
            if (membership != null){
                return@run failure(DeclineChannelInvitationError.UserIsAlreadyMember)
            }

            success(Unit)
        }
    }

    fun createMessage(
        channelId: Int,
        userId: Int,
        content: String,
    ): CreateMessageResult =
        transactionManager.run {
            it.channelsRepository.getChannel(channelId, userId)
                ?: return@run failure(CreateMessageError.ChannelDoesNotExist)

            val membership =
                it.channelsRepository.getMembership(channelId, userId)
                    ?: return@run failure(CreateMessageError.UserIsNotMember)

            if (membership.role == MembershipRole.VIEWER) {
                return@run failure(CreateMessageError.UserIsNotAuthorizedToWrite)
            }

            success(it.channelsRepository.createMessage(channelId, userId, content, clock))
        }

    fun listMessages(
        channelId: Int,
        userId: Int,
    ): GetMessagesResult =
        transactionManager.run {
            val channel = it.channelsRepository.getChannel(channelId, userId)
            if (channel == null) {
                logger.error("Channel $channelId does not exist")
                return@run failure(GetMessagesError.ChannelDoesNotExist)
            }

            val membership = it.channelsRepository.getMembership(channelId, userId)
            if (membership == null) {
                logger.error("User $userId is not a member of channel $channelId")
                return@run failure(GetMessagesError.UserIsNotMember)
            }

            val messages = it.channelsRepository.getMessages(channelId)
            return@run success(messages)
        }

    fun createChannelInvitation(
        channelId: Int,
        userId: Int,
        invitedUsername: String,
        role: MembershipRole,
    ): InviteMemberResult {
        if (role == MembershipRole.OWNER) {
            return failure(InviteMemberError.ForbiddenRole)
        }
        return transactionManager.run {
            it.channelsRepository.getChannel(channelId, userId)
                ?: return@run failure(InviteMemberError.ChannelDoesNotExist)

            val invitedUser =
                it.usersRepository.getUser(invitedUsername)
                    ?: return@run failure(InviteMemberError.InviteeDoesNotExist)

            val membership =
                it.channelsRepository.getMembership(channelId, userId)
                    ?: return@run failure(InviteMemberError.MembershipDoesNotExist)

            if (it.channelsRepository.getMembership(channelId, invitedUser.userId) != null) {
                return@run failure(InviteMemberError.MembershipAlreadyExists)
            }

            if (!channelDomain.isHigherRole(membership.role, role)) {
                return@run failure(InviteMemberError.ForbiddenRole)
            }

            val invite = it.channelsRepository.createChannelInvitation(
                channelId,
                userId,
                invitedUser.userId,
                role.role,
                clock,
            )
            success(invite)
        }
    }

    fun listInvitations(userId: Int): GetInvitationsResult =
        transactionManager.run {
            val invitations = it.channelsRepository.listInvitations(userId)
            success(invitations)
        }

    fun deleteMembership(
        channelId: Int,
        userId: Int,
    ): DeleteMembershipResult =
        transactionManager.run {
            it.channelsRepository.getChannel(channelId, userId)
                ?: return@run failure(DeleteMembershipError.ChannelDoesNotExist)

            val membership =
                it.channelsRepository.getMembership(channelId, userId)
                    ?: return@run failure(DeleteMembershipError.UserIsNotMember)

            if (membership.role == MembershipRole.OWNER) {
                return@run failure(DeleteMembershipError.UserIsOwner)
            }

            success(it.channelsRepository.deleteMembership(channelId, userId))
        }

    companion object {
        private val logger = LoggerFactory.getLogger(ChannelService::class.java)
    }
}
