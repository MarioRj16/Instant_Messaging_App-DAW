package com.example.messagingapp.services

import com.example.messagingapp.TestClock
import com.example.messagingapp.bootstrapChannel
import com.example.messagingapp.bootstrapUser
import com.example.messagingapp.channelDomain
import com.example.messagingapp.clearDatabase
import com.example.messagingapp.domain.Channel
import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.http.model.output.MessageOutputModel
import com.example.messagingapp.jdbi
import com.example.messagingapp.transactionManager
import com.example.messagingapp.utils.Either
import com.example.messagingapp.utils.PaginatedResponse
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ChannelsServiceTests {
    @AfterEach
    fun tearDown() {
        clearDatabase(jdbi)
    }

    @Test
    fun `Channel can be created`() {
        val testClock = TestClock()
        val channelsService = createChannelsService(testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelName = "channel"
        val isPublic = true

        val channelCreationResult = channelsService.createChannel(channelName, userId, isPublic)
        when (channelCreationResult) {
            is Either.Left -> throw AssertionError("Channel creation failed: ${channelCreationResult.value}")
            is Either.Right -> assertIs<Int>(channelCreationResult.value)
        }

        transactionManager.run {
            val memberships = it.channelsRepository.listMemberships(channelCreationResult.value)
            assertEquals(1, memberships.totalSize)
            assertEquals(channelCreationResult.value, memberships.data[0].channelId)
            assertEquals(userId, memberships.data[0].member.userId)
            assertEquals(MembershipRole.OWNER, memberships.data[0].role)
        }
    }

    @Test
    fun `Channel cannot be created with invalid name`() {
        val testClock = TestClock()
        val channelsService = createChannelsService(testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelName = ""
        val isPublic = true

        when (val channelCreationResult = channelsService.createChannel(channelName, userId, isPublic)) {
            is Either.Left -> assertIs<ChannelCreationError.ChannelNameIsNotValid>(channelCreationResult.value)
            is Either.Right -> throw AssertionError("Channel creation failed: ${channelCreationResult.value}")
        }
    }

    @Test
    fun `Channel cannot be created with existing name`() {
        val testClock = TestClock()
        val channelsService = createChannelsService(testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelName = "channel"
        val isPublic = true

        channelsService.createChannel(channelName, userId, isPublic)

        when (val channelCreationResult = channelsService.createChannel(channelName, userId, isPublic)) {
            is Either.Left -> assertIs<ChannelCreationError.ChannelNameAlreadyExists>(channelCreationResult.value)
            is Either.Right -> throw AssertionError("Channel creation failed: ${channelCreationResult.value}")
        }
    }

    @Test
    fun `Channel can be retrieved`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelName = "channel"
        val channelsService = createChannelsService(testClock)
        val channelId = bootstrapChannel(userId, channelName)

        when (val channelGetResult = channelsService.getChannel(channelId, userId)) {
            is Either.Left -> throw AssertionError("Channel get failed: ${channelGetResult.value}")
            is Either.Right -> {
                assertIs<Channel>(channelGetResult.value)
                assertEquals(channelGetResult.value.channelId, channelId)
                assertEquals(channelGetResult.value.channelName, channelName)
                assertEquals(channelGetResult.value.owner.userId, userId)
                assertEquals(channelGetResult.value.members.size, 1)
                assertEquals(channelGetResult.value.members[0].member.userId, userId)
            }
        }
    }

    @Test
    fun `Channel cannot be retrieved if it does not exist`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = Int.MAX_VALUE
        val channelsService = createChannelsService(testClock)

        when (val channelGetResult = channelsService.getChannel(channelId, userId)) {
            is Either.Left -> assertIs<ChannelGetError.ChannelNotFound>(channelGetResult.value)
            is Either.Right -> throw AssertionError("Channel get failed: ${channelGetResult.value}")
        }
    }

    @Test
    fun `Joined channels can be listed`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val userId2 = bootstrapUser(testClock = testClock)
        val channelsService = createChannelsService(testClock)

        bootstrapChannel(userId)
        bootstrapChannel(userId2)

        when (val channelGetResult = channelsService.listJoinedChannels(userId)) {
            is Either.Left -> throw AssertionError("Channel get failed: ${channelGetResult.value}")
            is Either.Right -> {
                assertIs<PaginatedResponse<Channel>>(channelGetResult.value)
                assertEquals(1, channelGetResult.value.totalSize)
            }
        }
    }

    @Test
    fun `Channels can be searched`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val userId2 = bootstrapUser(testClock = testClock)
        val channelsService = createChannelsService(testClock)
        bootstrapChannel(userId, "channel")
        bootstrapChannel(userId, "channel2")
        bootstrapChannel(userId2, "channel3")
        bootstrapChannel(userId2, "channel4", false)

        when (val channelGetResult = channelsService.searchChannels(userId, "")) {
            is Either.Left -> throw AssertionError("Channel get failed: ${channelGetResult.value}")
            is Either.Right -> {
                assertIs<PaginatedResponse<Channel>>(channelGetResult.value)
                assertEquals(3, channelGetResult.value.totalSize)
            }
        }

        when (val channelGetResult = channelsService.searchChannels(userId, "channel2")) {
            is Either.Left -> throw AssertionError("Channel get failed: ${channelGetResult.value}")
            is Either.Right -> {
                assertIs<PaginatedResponse<Channel>>(channelGetResult.value)
                assertEquals(1, channelGetResult.value.totalSize)
            }
        }
    }

    @Test
    fun `User can join public channel`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId, isPublic = true)
        val channelsService = createChannelsService(testClock)

        when (val joinResult = channelsService.joinPublicChannel(channelId, userId)) {
            is Either.Left -> throw AssertionError("Joining channel failed: ${joinResult.value}")
            is Either.Right -> {
                assertIs<Unit>(joinResult.value)
            }
        }

        transactionManager.run {
            val membership = it.channelsRepository.getMembership(channelId, userId)
            assertNotNull(membership)
        }
    }

    @Test
    fun `User cannot join channel if channel is not found`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = Int.MAX_VALUE
        val channelsService = createChannelsService(testClock)

        when (val joinResult = channelsService.joinPublicChannel(channelId, userId)) {
            is Either.Left -> assertIs<JoinChannelError.ChannelNotFound>(joinResult.value)
            is Either.Right -> throw AssertionError("Joining channel failed: ${joinResult.value}")
        }
    }

    @Test
    fun `User cannot join channel if user is not public`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(userId, isPublic = false)
        val channelsService = createChannelsService(testClock)

        when (val joinResult = channelsService.joinPublicChannel(channelId, userId)) {
            is Either.Left -> assertIs<JoinChannelError.ChannelIsNotPublic>(joinResult.value)
            is Either.Right -> throw AssertionError("Joining channel failed: ${joinResult.value}")
        }
    }

    @Test
    fun `User cannot join channel if user is already a member`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(userId, isPublic = true)
        val channelsService = createChannelsService(testClock)

        when (val joinResult = channelsService.joinPublicChannel(channelId, userId)) {
            is Either.Left -> assertIs<JoinChannelError.UserIsAlreadyMember>(joinResult.value)
            is Either.Right -> throw AssertionError("Joining channel failed: ${joinResult.value}")
        }
    }

    @Test
    fun `User can accept channel invitation and join channel`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = ownerId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        transactionManager.run {
            it.channelsRepository.createChannelInvitation(channelId, ownerId, userId, MembershipRole.MEMBER.role, testClock)
        }

        when (val acceptResult = channelsService.acceptChannelInvitation(channelId, userId)) {
            is Either.Left -> throw AssertionError("Accepting invitation failed: ${acceptResult.value}")
            is Either.Right -> {
                assertIs<Unit>(acceptResult.value)
            }
        }

        transactionManager.run {
            val membership = it.channelsRepository.getMembership(channelId, userId)
            assertNotNull(membership)

            val invitation = it.channelsRepository.getInvitation(channelId, userId)
            assertNull(invitation)
        }
    }

    @Test
    fun `User cannot accept channel invitation if invitation is not found`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = ownerId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        when (val acceptResult = channelsService.acceptChannelInvitation(channelId, userId)) {
            is Either.Left -> assertIs<AcceptChannelInvitationError.InvitationNotFound>(acceptResult.value)
            is Either.Right -> throw AssertionError("Accepting invitation failed: ${acceptResult.value}")
        }
    }

    @Test
    fun `User cannot accept channel invitation if user is already a member`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = ownerId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        transactionManager.run {
            it.channelsRepository.createMembership(userId, channelId, testClock, MembershipRole.MEMBER.role)
            it.channelsRepository.createChannelInvitation(channelId, ownerId, userId, MembershipRole.MEMBER.role, testClock)
        }

        when (val acceptResult = channelsService.acceptChannelInvitation(channelId, userId)) {
            is Either.Left -> assertIs<AcceptChannelInvitationError.UserIsAlreadyMember>(acceptResult.value)
            is Either.Right -> throw AssertionError("Accepting invitation failed: ${acceptResult.value}")
        }
    }

    @Test
    fun `User can decline channel invitation`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = ownerId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        transactionManager.run {
            it.channelsRepository.createChannelInvitation(channelId, ownerId, userId, MembershipRole.MEMBER.role, testClock)
        }

        when (val declineResult = channelsService.declineChannelInvitation(channelId, userId)) {
            is Either.Left -> throw AssertionError("Declining invitation failed: ${declineResult.value}")
            is Either.Right -> {
                assertIs<Unit>(declineResult.value)
            }
        }

        transactionManager.run {
            val invitation = it.channelsRepository.getInvitation(channelId, userId)
            assertNull(invitation)
        }
    }

    @Test
    fun `User cannot decline channel invitation if invitation is not found`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = ownerId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        when (val declineResult = channelsService.declineChannelInvitation(channelId, userId)) {
            is Either.Left -> assertIs<DeclineChannelInvitationError.InvitationNotFound>(declineResult.value)
            is Either.Right -> throw AssertionError("Declining invitation failed: ${declineResult.value}")
        }
    }

    @Test
    fun `User cannot decline channel invitation if user is already a member`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = ownerId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        transactionManager.run {
            it.channelsRepository.createMembership(userId, channelId, testClock, MembershipRole.MEMBER.role)
            it.channelsRepository.createChannelInvitation(channelId, ownerId, userId, MembershipRole.MEMBER.role, testClock)
        }

        when (val declineResult = channelsService.declineChannelInvitation(channelId, userId)) {
            is Either.Left -> assertIs<DeclineChannelInvitationError.UserIsAlreadyMember>(declineResult.value)
            is Either.Right -> throw AssertionError("Declining invitation failed: ${declineResult.value}")
        }
    }

    @Test
    fun `Membership can be deleted`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = ownerId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        transactionManager.run {
            it.channelsRepository.createMembership(userId, channelId, testClock, MembershipRole.MEMBER.role)
        }

        when (val deleteResult = channelsService.deleteMembership(channelId, userId)) {
            is Either.Left -> throw AssertionError("Deleting membership failed: ${deleteResult.value}")
            is Either.Right -> {
                assertIs<Unit>(deleteResult.value)
            }
        }
    }

    @Test
    fun `Membership cannot be deleted if channel is not found`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = Int.MAX_VALUE
        val channelsService = createChannelsService(testClock)

        when (val deleteResult = channelsService.deleteMembership(channelId, userId)) {
            is Either.Left -> assertIs<DeleteMembershipError.ChannelNotFound>(deleteResult.value)
            is Either.Right -> throw AssertionError("Deleting membership failed: ${deleteResult.value}")
        }
    }

    @Test
    fun `Membership cannot be deleted if user is not a member`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId)
        val channelsService = createChannelsService(testClock)

        when (val deleteResult = channelsService.deleteMembership(channelId, userId)) {
            is Either.Left -> assertIs<DeleteMembershipError.UserIsNotMember>(deleteResult.value)
            is Either.Right -> throw AssertionError("Deleting membership failed: ${deleteResult.value}")
        }
    }

    @Test
    fun `Membership cannot be deleted if user is the owner of the channel`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)

        when (val deleteResult = channelsService.deleteMembership(channelId, userId)) {
            is Either.Left -> assertIs<DeleteMembershipError.UserIsOwner>(deleteResult.value)
            is Either.Right -> throw AssertionError("Deleting membership failed: ${deleteResult.value}")
        }
    }

    @Test
    fun `Message can be created`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = userId, clock = testClock)
        val channelsService = createChannelsService(testClock)
        val message = "content"
        when (val messageResult = channelsService.createMessage(channelId, userId, message)) {
            is Either.Left -> throw AssertionError("Sending message failed: ${messageResult.value}")
            is Either.Right -> {
                assertIs<Int>(messageResult.value)
            }
        }
    }

    @Test
    fun `Messages cannot be created if channel is not found`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = Int.MAX_VALUE
        val channelsService = createChannelsService(testClock)
        val message = "content"
        when (val messageResult = channelsService.createMessage(channelId, userId, message)) {
            is Either.Left -> assertIs<CreateMessageError.ChannelNotFound>(messageResult.value)
            is Either.Right -> throw AssertionError("Sending message failed: ${messageResult.value}")
        }
    }

    @Test
    fun `Messages cannot be created if membership is not found`() {
        val testClock = TestClock()
        val channelsService = createChannelsService(testClock)
        val userId = bootstrapUser(testClock = testClock)
        val userId2 = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = userId, clock = testClock)
        val message = "content"
        when (val messageResult = channelsService.createMessage(channelId, userId2, message)) {
            is Either.Left -> assertIs<CreateMessageError.UserIsNotMember>(messageResult.value)
            is Either.Right -> throw AssertionError("Sending message failed: ${messageResult.value}")
        }
    }

    @Test
    fun `Messages cannot be created if user is not authorized to write`() {
        val testClock = TestClock()
        val channelsService = createChannelsService(testClock)
        val userId = bootstrapUser(testClock = testClock)
        val userId2 = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = userId, clock = testClock)
        val message = "content"

        transactionManager.run {
            it.channelsRepository.createMembership(userId2, channelId, testClock, MembershipRole.VIEWER.role)
        }

        when (val messageResult = channelsService.createMessage(channelId, userId2, message)) {
            is Either.Left -> assertIs<CreateMessageError.UserIsNotAuthorizedToWrite>(messageResult.value)
            is Either.Right -> throw AssertionError("Sending message failed: ${messageResult.value}")
        }
    }

    @Test
    fun `Messages can be listed`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = userId, clock = testClock)
        val channelsService = createChannelsService(testClock)
        val numberOfMessages = 3

        repeat(numberOfMessages) {
            channelsService.createMessage(channelId, userId, "content")
        }

        when (val messagesResult = channelsService.listMessages(channelId, userId)) {
            is Either.Left -> throw AssertionError("Getting messages failed: ${messagesResult.value}")
            is Either.Right -> {
                assertIs<List<MessageOutputModel>>(messagesResult.value)
                assertEquals(numberOfMessages, messagesResult.value.size)
            }
        }
    }

    @Test
    fun `Messages cannot be listed if channel is not found`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = Int.MAX_VALUE
        val channelsService = createChannelsService(testClock)

        when (val messagesResult = channelsService.listMessages(channelId, userId)) {
            is Either.Left -> assertIs<GetMessagesError.ChannelNotFound>(messagesResult.value)
            is Either.Right -> throw AssertionError("Getting messages failed: ${messagesResult.value}")
        }
    }

    @Test
    fun `Messages cannot be listed if membership is not found`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val userId2 = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = userId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        when (val messagesResult = channelsService.listMessages(channelId, userId2)) {
            is Either.Left -> assertIs<GetMessagesError.UserIsNotMember>(messagesResult.value)
            is Either.Right -> throw AssertionError("Getting messages failed: ${messagesResult.value}")
        }
    }

    @Test
    fun `Can invite user to channel`() {
        val testClock = TestClock()
        val inviteeName = "invitee"
        val inviterId = bootstrapUser(testClock = testClock)
        val inviteeId = bootstrapUser(inviteeName, testClock = testClock)
        val channelId = bootstrapChannel(ownerId = inviterId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        when (val invitationResult = channelsService.createChannelInvitation(channelId, inviterId, inviteeName, MembershipRole.MEMBER)) {
            is Either.Left -> throw AssertionError("Invitation failed: ${invitationResult.value}")
            is Either.Right -> {
                assertIs<Int>(invitationResult.value)
            }
        }

        when (val invitations = channelsService.listInvitations(inviteeId)) {
            is Either.Left -> throw AssertionError("Getting invitations failed: ${invitations.value}")
            is Either.Right -> {
                assertIs<PaginatedResponse<ChannelInvitation>>(invitations.value)
                assertEquals(1, invitations.value.totalSize)
                assertEquals(invitations.value.data[0].channel.channelId, channelId)
                assertEquals(invitations.value.data[0].inviter.userId, inviterId)
                assertEquals(invitations.value.data[0].inviteeId, inviteeId)
            }
        }
    }

    @Test
    fun `Cannot invite user to channel as owner`() {
        val inviteeName = "invitee"
        val testClock = TestClock()
        bootstrapUser(inviteeName, testClock = testClock)
        val inviterId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = inviterId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        when (val invitationResult = channelsService.createChannelInvitation(channelId, inviterId, inviteeName, MembershipRole.OWNER)) {
            is Either.Left -> assertIs<InviteMemberError.ForbiddenRole>(invitationResult.value)
            is Either.Right -> throw AssertionError("Invitation failed: ${invitationResult.value}")
        }
    }

    @Test
    fun `Cannot invite to user to channel if invitee is not found`() {
        val testClock = TestClock()
        val inviterId = bootstrapUser(testClock = testClock)
        val inviteeName = "invitee"
        val channelId = bootstrapChannel(ownerId = inviterId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        when (val invitationResult = channelsService.createChannelInvitation(channelId, inviterId, inviteeName, MembershipRole.MEMBER)) {
            is Either.Left -> assertIs<InviteMemberError.InviteeNotFound>(invitationResult.value)
            is Either.Right -> throw AssertionError("Invitation failed: ${invitationResult.value}")
        }
    }

    @Test
    fun `Cannot invite to user to channel twice`() {
        val testClock = TestClock()
        val inviteeName = "invitee"
        val inviterId = bootstrapUser(testClock = testClock)
        bootstrapUser(inviteeName, testClock = testClock)
        val channelId = bootstrapChannel(ownerId = inviterId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        channelsService.createChannelInvitation(channelId, inviterId, inviteeName, MembershipRole.MEMBER)

        when (val invitationResult = channelsService.createChannelInvitation(channelId, inviterId, inviteeName, MembershipRole.MEMBER)) {
            is Either.Left -> assertIs<InviteMemberError.InvitationAlreadyExists>(invitationResult.value)
            is Either.Right -> throw AssertionError("Invitation failed: ${invitationResult.value}")
        }
    }

    @Test
    fun `Cannot invite user to channel if channel is not found`() {
        val testClock = TestClock()
        val inviteeName = "invitee"
        val inviterId = bootstrapUser(testClock = testClock)
        bootstrapUser(inviteeName, testClock = testClock)
        val channelId = Int.MAX_VALUE
        val channelsService = createChannelsService(testClock)

        when (val invitationResult = channelsService.createChannelInvitation(channelId, inviterId, inviteeName, MembershipRole.MEMBER)) {
            is Either.Left -> assertIs<InviteMemberError.ChannelNotFound>(invitationResult.value)
            is Either.Right -> throw AssertionError("Invitation failed: ${invitationResult.value}")
        }
    }

    @Test
    fun `Cannot invite user to channel if inviter membership is not found`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val inviterId = bootstrapUser(testClock = testClock)
        val inviteeName = "invitee"
        bootstrapUser(inviteeName, testClock = testClock)
        val channelId = bootstrapChannel(ownerId = ownerId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        when (val invitationResult = channelsService.createChannelInvitation(channelId, inviterId, inviteeName, MembershipRole.MEMBER)) {
            is Either.Left -> assertIs<InviteMemberError.MembershipNotFound>(invitationResult.value)
            is Either.Right -> throw AssertionError("Invitation failed: ${invitationResult.value}")
        }
    }

    @Test
    fun `Cannot invite user to channel if invitee has a higher role than inviter`() {
        val testClock = TestClock()
        val ownerId = bootstrapUser(testClock = testClock)
        val inviterId = bootstrapUser(testClock = testClock)
        val inviteeName = "invitee"
        bootstrapUser(username = inviteeName, testClock = testClock)
        val channelId = bootstrapChannel(ownerId = ownerId, clock = testClock)
        val channelsService = createChannelsService(testClock)

        transactionManager.run {
            it.channelsRepository.createMembership(inviterId, channelId, testClock, MembershipRole.VIEWER.role)
        }

        when (val invitationResult = channelsService.createChannelInvitation(channelId, inviterId, inviteeName, MembershipRole.MEMBER)) {
            is Either.Left -> assertIs<InviteMemberError.ForbiddenRole>(invitationResult.value)
            is Either.Right -> throw AssertionError("Invitation failed: ${invitationResult.value}")
        }
    }

    @Test
    fun `Channel invitations can be listed`() {
        val testClock = TestClock()
        val userId = bootstrapUser(testClock = testClock)
        val channelId = bootstrapChannel(ownerId = userId, clock = testClock)
        val channelId2 = bootstrapChannel(ownerId = userId, clock = testClock)
        val inviteeName = "invitee"
        val inviteeId = bootstrapUser(inviteeName, testClock = testClock)
        val channelsService = createChannelsService(testClock)

        channelsService.createChannelInvitation(channelId, userId, inviteeName, MembershipRole.MEMBER)
        channelsService.createChannelInvitation(channelId2, userId, inviteeName, MembershipRole.MEMBER)

        when (val invitationsResult = channelsService.listInvitations(inviteeId)) {
            is Either.Left -> throw AssertionError("Getting invitations failed: ${invitationsResult.value}")
            is Either.Right -> {
                assertIs<PaginatedResponse<ChannelInvitation>>(invitationsResult.value)
                assertEquals(2, invitationsResult.value.totalSize)
            }
        }
    }

    companion object {
        private fun createChannelsService(testClock: TestClock) = ChannelService(transactionManager, channelDomain, testClock)
    }
}
