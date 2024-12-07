package com.example.messagingapp.services

import com.example.messagingapp.DEFAULT_PAGE
import com.example.messagingapp.DEFAULT_PAGE_SIZE
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
    ): ChannelCreationResult {
        logger.info("Creating channel $channelName")
        return transactionManager.run {
            if (!channelDomain.isValidName(channelName)) {
                val error = ChannelCreationError.ChannelNameIsNotValid
                logger.error(error.message)
                return@run failure(error)
            }

            val channels = it.channelsRepository.searchChannels(userId, channelName).data
            if (
                channels.any { channel -> channel.channelName.lowercase() == channelName.lowercase() }
            ) {
                val error = ChannelCreationError.ChannelNameAlreadyExists
                logger.error(error.message)
                return@run failure(error)
            }

            val channelId = it.channelsRepository.createChannel(channelName, isPublic, userId, clock)
            it.channelsRepository.createMembership(userId, channelId, clock, MembershipRole.OWNER.role)
            success(channelId)
        }
    }

    fun getChannel(
        channelId: Int,
        userId: Int,
    ): ChannelGetResult {
        logger.info("Getting channel $channelId")
        return transactionManager.run {
            val channel = it.channelsRepository.getChannel(channelId, userId)
            if (channel == null) {
                val error = ChannelGetError.ChannelNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            success(channel)
        }
    }

    fun listJoinedChannels(
        userId: Int,
        page: Int = DEFAULT_PAGE.toInt(),
        pageSize: Int = DEFAULT_PAGE_SIZE.toInt(),
    ): GetJoinedChannelsResult {
        logger.info("Getting joined channels for user $userId")
        return transactionManager.run {
            val channels = it.channelsRepository.listJoinedChannels(userId, page, pageSize)
            success(channels)
        }
    }

    fun searchChannels(
        userId: Int,
        channelName: String,
        page: Int = DEFAULT_PAGE.toInt(),
        pageSize: Int = DEFAULT_PAGE_SIZE.toInt(),
    ): SearchChannelsResult {
        logger.info("Searching channels for user $userId with name $channelName")
        return transactionManager.run {
            val channels = it.channelsRepository.searchChannels(userId, channelName, page, pageSize)
            success(channels)
        }
    }

    fun joinPublicChannel(
        channelId: Int,
        userId: Int,
    ): JoinChannelResult {
        logger.info("Joining public channel $channelId")
        return transactionManager.run {
            val channel = it.channelsRepository.getChannel(channelId, userId)
            if (channel == null) {
                val error = JoinChannelError.ChannelNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            if (!channel.isPublic) {
                val error = JoinChannelError.ChannelIsNotPublic
                logger.error(error.message)
                return@run failure(error)
            }

            if (it.channelsRepository.getMembership(channelId, userId) != null) {
                val error = JoinChannelError.UserIsAlreadyMember
                logger.error(error.message)
                return@run failure(error)
            }

            success(it.channelsRepository.createMembership(userId, channelId, clock, MembershipRole.MEMBER.role))
        }
    }

    fun acceptChannelInvitation(
        channelId: Int,
        userId: Int,
    ): AcceptChannelInvitationResult {
        logger.info("Accepting channel invitation $channelId")
        return transactionManager.run {
            val invitation = it.channelsRepository.getInvitation(channelId, userId)
            if (invitation == null) {
                val error = AcceptChannelInvitationError.InvitationNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            // We'll delete the invitation even if the user is already a member
            it.channelsRepository.deleteInvitation(invitation.channelInvitationId)

            val membership = it.channelsRepository.getMembership(channelId, userId)
            if (membership != null) {
                val error = AcceptChannelInvitationError.UserIsAlreadyMember
                logger.error(error.message)
                return@run failure(error)
            }

            it.channelsRepository.createMembership(userId, channelId, clock, invitation.role.role)
            success(Unit)
        }
    }

    fun declineChannelInvitation(
        channelId: Int,
        userId: Int,
    ): DeclineChannelInvitationResult {
        logger.info("Declining channel invitation $channelId")
        return transactionManager.run {
            val invitation = it.channelsRepository.getInvitation(channelId, userId)
            if (invitation == null) {
                val error = DeclineChannelInvitationError.InvitationNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            // We'll delete the invitation even if the user is already a member
            it.channelsRepository.deleteInvitation(invitation.channelInvitationId)

            val membership = it.channelsRepository.getMembership(channelId, userId)
            if (membership != null) {
                val error = DeclineChannelInvitationError.UserIsAlreadyMember
                logger.error(error.message)
                return@run failure(error)
            }

            success(Unit)
        }
    }

    fun createMessage(
        channelId: Int,
        userId: Int,
        content: String,
    ): CreateMessageResult {
        logger.info("Creating message in channel $channelId")
        return transactionManager.run {
            val channel = it.channelsRepository.getChannel(channelId, userId)
            if (channel == null) {
                val error = CreateMessageError.ChannelNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            val membership = it.channelsRepository.getMembership(channelId, userId)
            if (membership == null) {
                val error = CreateMessageError.UserIsNotMember
                logger.error(error.message)
                return@run failure(error)
            }

            if (membership.role == MembershipRole.VIEWER) {
                val error = CreateMessageError.UserIsNotAuthorizedToWrite
                logger.error(error.message)
                return@run failure(error)
            }

            success(it.channelsRepository.createMessage(channelId, userId, content, clock))
        }
    }

    fun listMessages(
        channelId: Int,
        userId: Int,
    ): GetMessagesResult {
        logger.info("Getting messages for channel $channelId")
        return transactionManager.run {
            val channel = it.channelsRepository.getChannel(channelId, userId)
            if (channel == null) {
                val error = GetMessagesError.ChannelNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            val membership = it.channelsRepository.getMembership(channelId, userId)
            if (membership == null) {
                val error = GetMessagesError.UserIsNotMember
                logger.error(error.message)
                return@run failure(error)
            }

            val messages = it.channelsRepository.listMessages(channelId)
            return@run success(messages)
        }
    }

    fun createChannelInvitation(
        channelId: Int,
        userId: Int,
        inviteeUsername: String,
        role: MembershipRole,
    ): InviteMemberResult {
        logger.info("Inviting user $inviteeUsername to channel $channelId")
        if (role == MembershipRole.OWNER) {
            logger.error("User $userId is trying to invite a user with the role OWNER")
            return failure(InviteMemberError.ForbiddenRole)
        }
        return transactionManager.run {
            val channel = it.channelsRepository.getChannel(channelId, userId)
            if (channel == null) {
                val error = InviteMemberError.ChannelNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            val invitedUser = it.usersRepository.getUser(inviteeUsername)
            if (invitedUser == null) {
                val error = InviteMemberError.InviteeNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            val membership = it.channelsRepository.getMembership(channelId, userId)
            if (membership == null) {
                val error = InviteMemberError.MembershipNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            if (it.channelsRepository.getMembership(channelId, invitedUser.userId) != null) {
                val error = InviteMemberError.MembershipAlreadyExists
                logger.error(error.message)
                return@run failure(error)
            }

            if (!channelDomain.isHigherRole(membership.role, role)) {
                logger.error("User $userId is trying to invite a user with a higher role")
                return@run failure(InviteMemberError.ForbiddenRole)
            }

            val invite =
                it.channelsRepository.createChannelInvitation(
                    channelId,
                    userId,
                    invitedUser.userId,
                    role.role,
                    clock,
                )
            success(invite)
        }
    }

    fun listInvitations(
        userId: Int,
        page: Int = DEFAULT_PAGE.toInt(),
        pageSize: Int = DEFAULT_PAGE_SIZE.toInt(),
    ): GetInvitationsResult {
        logger.info("Getting invitations for user $userId")
        return transactionManager.run {
            val invitations = it.channelsRepository.listInvitations(userId, page, pageSize)
            success(invitations)
        }
    }

    fun deleteMembership(
        channelId: Int,
        userId: Int,
    ): DeleteMembershipResult {
        logger.info("Deleting membership for user $userId in channel $channelId")
        return transactionManager.run {
            if (it.channelsRepository.getChannel(channelId, userId) == null) {
                val error = DeleteMembershipError.ChannelNotFound
                logger.error(error.message)
                return@run failure(error)
            }

            val membership = it.channelsRepository.getMembership(channelId, userId)
            if (membership == null) {
                val error = DeleteMembershipError.UserIsNotMember
                logger.error(error.message)
                return@run failure(error)
            }

            if (membership.role == MembershipRole.OWNER) {
                val error = DeleteMembershipError.UserIsOwner
                logger.error(error.message)
                return@run failure(error)
            }

            success(it.channelsRepository.deleteMembership(channelId, userId))
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ChannelService::class.java)
    }
}
