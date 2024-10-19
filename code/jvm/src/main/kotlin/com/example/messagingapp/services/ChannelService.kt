package com.example.messagingapp.services

import com.example.messagingapp.domain.ChannelDomain
import com.example.messagingapp.domain.InviteStatus
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.repository.TransactionManager
import com.example.messagingapp.utils.failure
import com.example.messagingapp.utils.success
import kotlinx.datetime.Clock
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
        if (!channelDomain.isValidName(channelName)) {
            failure(ChannelCreationError.NameIsNotValid)
        }
        return transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(ChannelCreationError.UserDoesNotExist)

            val channelId = it.channelsRepository.createChannel(channelName, isPublic, userId)
            success(channelId)
        }
    }

    fun getChannel(
        channelId: Long,
        userId: Int,
    ): ChannelGetResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(ChannelGetError.UserDoesNotExist)

            val channel =
                it.channelsRepository.getChannel(channelId, userId)
                    ?: return@run failure(ChannelGetError.ChannelDoesNotExist)

            success(channel)
        }

    fun getJoinedChannels(userId: Int): GetJoinedChannelsResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(GetJoinedChannelsError.UserDoesNotExist)

            val channels = it.channelsRepository.getJoinedChannels(userId)
            success(channels)
        }

    // TODO(COULD ADD NAME OF THE CHANNEL TO SEARCH)
    fun searchChannels(userId: Int): SearchChannelsResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(SearchChannelsError.UserDoesNotExist)

            val channels = it.channelsRepository.searchChannels()
            success(channels)
        }

    fun joinChannel(
        channelId: Long,
        userId: Int,
    ): JoinChannelResult {
        return transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(JoinChannelError.UserDoesNotExist)

            val channel =
                it.channelsRepository.getChannel(channelId, userId)
                    ?: return@run failure(JoinChannelError.ChannelDoesNotExist)

            if (!channel.isPublic) {
                return@run failure(JoinChannelError.ChannelIsNotPublic)
            }

            if (it.channelsRepository.getMembership(channelId, userId) != null) {
                return@run failure(JoinChannelError.UserIsAlreadyMember)
            }

            success(it.channelsRepository.joinChannel(channelId, userId))
        }
    }

    fun getMessages(
        channelId: Long,
        userId: Int,
    ): GetMessagesResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(GetMessagesError.UserDoesNotExist)

            it.channelsRepository.getChannel(channelId, userId)
                ?: return@run failure(GetMessagesError.ChannelDoesNotExist)

            it.channelsRepository.getMembership(channelId, userId)
                ?: return@run failure(GetMessagesError.UserIsNotMember)

            val messages = it.channelsRepository.getMessages(channelId)
            success(messages)
        }

    fun sendMessage(
        channelId: Long,
        userId: Int,
        content: String,
    ): SendMessageResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(SendMessageError.UserDoesNotExist)

            it.channelsRepository.getChannel(channelId, userId)
                ?: return@run failure(SendMessageError.ChannelDoesNotExist)

            val membership =
                it.channelsRepository.getMembership(channelId, userId)
                    ?: return@run failure(SendMessageError.UserIsNotMember)

            if (membership.role == MembershipRole.VIEWER) {
                return@run failure(SendMessageError.UserIsNotAuthorizedToWrite)
            }

            success(it.channelsRepository.sendMessage(channelId, userId, content))
        }

    fun getMembership(
        channelId: Long,
        userId: Int,
    ): GetMembershipResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(GetMembershipError.UserDoesNotExist)

            it.channelsRepository.getChannel(channelId, userId)
                ?: return@run failure(GetMembershipError.ChannelDoesNotExist)

            val membership =
                it.channelsRepository.getMembership(channelId, userId)
                    ?: return@run failure(GetMembershipError.MembershipDoesNotExist)

            success(membership)
        }

    fun getMemberships(
        channelId: Long,
        userId: Int,
    ): GetMembershipsResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(GetMembershipsError.UserDoesNotExist)

            it.channelsRepository.getChannel(channelId, userId)
                ?: return@run failure(GetMembershipsError.ChannelDoesNotExist)

            it.channelsRepository.getMembership(channelId, userId)
                ?: return@run failure(GetMembershipsError.UserIsNotMember)
            val memberships = it.channelsRepository.getMemberships(channelId)

            success(memberships)
        }

    fun inviteMember(
        channelId: Long,
        userId: Int,
        invitedUsername: String,
        role: MembershipRole,
    ): InviteMemberResult {
        if (role == MembershipRole.OWNER) {
            return failure(InviteMemberError.CannotMakeMemberOwner)
        }
        return transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(InviteMemberError.UserDoesNotExist)

            it.channelsRepository.getChannel(channelId, userId)
                ?: return@run failure(InviteMemberError.ChannelDoesNotExist)

            val invitedUser =
                it.usersRepository.getUserByUsername(invitedUsername)
                    ?: return@run failure(InviteMemberError.InviteeDoesNotExist)

            val membership =
                it.channelsRepository.getMembership(channelId, userId)
                    ?: return@run failure(InviteMemberError.MembershipDoesNotExist)

            if (it.channelsRepository.getMembership(channelId, invitedUser.userId) != null) {
                return@run failure(InviteMemberError.MembershipAlreadyExists)
            }

            if (!channelDomain.isHigherRole(membership.role, role)) {
                return@run failure(InviteMemberError.CannotMakeInviteeHigherRole)
            }

            /**
             * Não adicionei esta condição porque ao fazer isto causa outro problema
             * Se não conseguirmos criar outro invite como é que fazemos se
             * o invite anterior der expire ou for recusado?
             if (it.channelsRepository.getInvitation()!= null){
             return@run failure(InviteMemberError.InviteAlreadyExists)
             }
             */
            val invite = it.channelsRepository.inviteMember(channelId, userId, invitedUser.userId, role.role, channelDomain.expirationDate)
            success(invite)
        }
    }

    fun getInvitations(userId: Int): GetInvitationsResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(GetInvitationsError.UserDoesNotExist)

            val invitations = it.channelsRepository.getInvitations(userId)
            success(invitations)
        }

    fun respondInvitation(
        userId: Int,
        invitationId: Long,
        response: Boolean,
    ): RespondInvitationResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(RespondInvitationError.UserDoesNotExist)

            val invitation =
                it.channelsRepository.getInvitationById(invitationId)
                    ?: return@run failure(RespondInvitationError.InvitationDoesNotExist)

            if (invitation.inviteeId != userId) {
                return@run failure(RespondInvitationError.InvitedUserDoesNotCoincide)
            }

            if (channelDomain.isExpired(invitation.expiresAt)) {
                return@run failure(RespondInvitationError.InvitationIsExpired)
            }

            if (invitation.status != InviteStatus.PENDING) {
                return@run failure(RespondInvitationError.InvitationIsNotPending)
            }
            // PODEMOS REMOVER OS OUTROS SE DER QUISERMOS MAIS EFICIENCIA

            // pnding ones

            val res =
                if (response) {
                    it.channelsRepository.acceptInvitation(invitationId, userId, invitation.channelId, invitation.role.role)
                } else {
                    it.channelsRepository.declineInvitation(invitationId)
                }
            success(res)
        }

    fun leaveChannel(
        channelId: Long,
        userId: Int,
    ): LeaveChannelResult =
        transactionManager.run {
            it.usersRepository.getUserById(userId)
                ?: return@run failure(LeaveChannelError.UserDoesNotExist)

            it.channelsRepository.getChannel(channelId, userId)
                ?: return@run failure(LeaveChannelError.ChannelDoesNotExist)

            val membership =
                it.channelsRepository.getMembership(channelId, userId)
                    ?: return@run failure(LeaveChannelError.UserIsNotMember)

            if (membership.role == MembershipRole.OWNER) {
                return@run failure(LeaveChannelError.UserIsOwner)
            }

            success(it.channelsRepository.leaveChannel(channelId, userId))
        }
}
