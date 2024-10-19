package com.example.messagingapp.services

import com.example.messagingapp.Environment
import com.example.messagingapp.TestClock
import com.example.messagingapp.domain.ChannelDomain
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.domain.UUIDTokenEncoder
import com.example.messagingapp.domain.UserDomain
import com.example.messagingapp.domain.UserDomainConfig
import com.example.messagingapp.generateRandomEmail
import com.example.messagingapp.generateRandomMessage
import com.example.messagingapp.generateRandomString
import com.example.messagingapp.http.model.ChannelInvitationOutput
import com.example.messagingapp.http.model.ChannelWithMembership
import com.example.messagingapp.http.model.MembershipOutput
import com.example.messagingapp.http.model.MessageOutput
import com.example.messagingapp.repository.jdbi.JdbiTransactionManager
import com.example.messagingapp.repository.jdbi.configureWithAppRequirements
import com.example.messagingapp.utils.Either
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class ChannelsServiceTests {
    // TODO(): Implement tests for the following functions:
    // CREATE CHANNEL ERROR

    @Test
    fun `Channel can be created`() {
        val testClock = TestClock()
        val channelsService = createChannelsService(testClock)
        val userId = bootstrapUser()

        val channelName = generateRandomString()
        val isPublic = true

        when (val channelCreationResult = channelsService.createChannel(channelName, userId, isPublic)) {
            is Either.Left -> throw AssertionError("Channel creation failed: ${channelCreationResult.value}")
            is Either.Right -> assertIs<Int>(channelCreationResult.value)
        }
    }

    @Test
    fun `Get channel`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)

        when (val channelGetResult = channelsService.getChannel(channelId.toLong(), userId)) {
            is Either.Left -> throw AssertionError("Channel get failed: ${channelGetResult.value}")
            is Either.Right -> {
                assertIs<ChannelWithMembership>(channelGetResult.value)
                assertTrue { channelGetResult.value.isMember }
                assertEquals(channelGetResult.value.channelId, channelId)
                assertEquals(channelGetResult.value.ownerId, userId)
            }
        }
    }

    @Test
    fun `Get joined channels`() {
        val userId = bootstrapUser()
        val otherUser = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)
        val secondChannelId = bootstrapChannel(otherUser)
        when (val channelGetResult = channelsService.getJoinedChannels(userId)) {
            is Either.Left -> throw AssertionError("Channel get failed: ${channelGetResult.value}")
            is Either.Right -> {
                assertIs<List<ChannelWithMembership>>(channelGetResult.value)
                assertEquals(1, channelGetResult.value.size)
                assertTrue { channelGetResult.value[0].isMember }
                assertEquals(channelGetResult.value[0].channelId, channelId)
                assertEquals(channelGetResult.value[0].ownerId, userId)
            }
        }
    }

    @Test
    fun `Search Channels`() {
        val userId = bootstrapUser()
        val ownerId = bootstrapUser()
        val testClock = TestClock()
        val channelsService = createChannelsService(testClock)
        val channel = bootstrapChannel(ownerId)
        when (val searchResult = channelsService.searchChannels(userId)) {
            is Either.Left -> throw AssertionError("Search failed: ${searchResult.value}")
            is Either.Right -> {
                assertIs<List<ChannelWithMembership>>(searchResult.value)
                assertTrue { 1 <= searchResult.value.size }
            }
        }
    }

    @Test
    fun `Join Channel`() {
        val userId = bootstrapUser()
        val otherUser = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(otherUser)
        val channelsService = createChannelsService(testClock)
        when (val joinResult = channelsService.joinChannel(channelId.toLong(), userId)) {
            is Either.Left -> throw AssertionError("Join failed: ${joinResult.value}")
            is Either.Right -> {
                assertIs<Unit>(joinResult.value)
            }
        }
    }

    @Test
    fun `Get Messages`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)
        val message = generateRandomString()
        val channelOther = bootstrapChannel(userId)
        when (val messageResult = channelsService.sendMessage(channelId.toLong(), userId, message)) {
            is Either.Left -> throw AssertionError("Sending message failed: ${messageResult.value}")
            is Either.Right -> {
                assertIs<Int>(messageResult.value)
            }
        }
        when (val messageResult = channelsService.sendMessage(channelId.toLong(), userId, message)) {
            is Either.Left -> throw AssertionError("Sending message failed: ${messageResult.value}")
            is Either.Right -> {
                assertIs<Int>(messageResult.value)
            }
        }
        when (val messageResult = channelsService.sendMessage(channelOther.toLong(), userId, message)) {
            is Either.Left -> throw AssertionError("Sending message failed: ${messageResult.value}")
            is Either.Right -> {
                assertIs<Int>(messageResult.value)
            }
        }
        when (val messagesResult = channelsService.getMessages(channelId.toLong(), userId)) {
            is Either.Left -> throw AssertionError("Getting messages failed: ${messagesResult.value}")
            is Either.Right -> {
                assertIs<List<MessageOutput>>(messagesResult.value)
                assertEquals(2, messagesResult.value.size)
                assertEquals(message, messagesResult.value[0].content)
            }
        }
    }

    @Test
    fun `Can create Invitation`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val inviteeName = generateRandomString()
        val inviteeId = bootstrapUser(inviteeName)
        val channelsService = createChannelsService(testClock)
        when (val invitationResult = channelsService.inviteMember(channelId.toLong(), userId, inviteeName, MembershipRole.MEMBER)) {
            is Either.Left -> throw AssertionError("Invitation failed: ${invitationResult.value}")
            is Either.Right -> {
                assertIs<Int>(invitationResult.value)
            }
        }
        // when(val getInvitation = channelsService.)
    }

    @Test
    fun `Get Invitations`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val inviteeName = generateRandomString()
        val inviteeId = bootstrapUser(inviteeName)
        val channelsService = createChannelsService(testClock)
        val invitationId =
            when (val invitationResult = channelsService.inviteMember(channelId.toLong(), userId, inviteeName, MembershipRole.MEMBER)) {
                is Either.Left -> throw AssertionError("Invitation failed: ${invitationResult.value}")
                is Either.Right -> {
                    assertIs<Int>(invitationResult.value)
                    invitationResult.value
                }
            }
        when (val invitationsResult = channelsService.getInvitations(inviteeId)) {
            is Either.Left -> throw AssertionError("Getting invitations failed: ${invitationsResult.value}")
            is Either.Right -> {
                assertIs<List<ChannelInvitationOutput>>(invitationsResult.value)
                assertEquals(1, invitationsResult.value.size)
                assertEquals(invitationsResult.value[0].channelId, channelId)
                assertEquals(invitationsResult.value[0].inviterId, userId)
                assertEquals(invitationsResult.value[0].inviteeId, inviteeId)
            }
        }
    }

    @Test
    fun `Can accept invitation`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val inviteeName = generateRandomString()
        val inviteeId = bootstrapUser(inviteeName)
        val channelsService = createChannelsService(testClock)
        val invitationId =
            when (val invitationResult = channelsService.inviteMember(channelId.toLong(), userId, inviteeName, MembershipRole.MEMBER)) {
                is Either.Left -> throw AssertionError("Invitation failed: ${invitationResult.value}")
                is Either.Right -> {
                    assertIs<Int>(invitationResult.value)
                    invitationResult.value
                }
            }
        when (val respondResult = channelsService.respondInvitation(inviteeId, invitationId.toLong(), true)) {
            is Either.Left -> throw AssertionError("Responding to invitation failed: ${respondResult.value}")
            is Either.Right -> {
                assertIs<Int>(respondResult.value)
            }
        }
        when (val respondResult = channelsService.respondInvitation(inviteeId, invitationId.toLong(), true)) {
            is Either.Right -> throw AssertionError(
                "Responding to invitation didn't fail after being already accepted: ${respondResult.value}",
            )
            is Either.Left -> {
                assertIs<RespondInvitationError.InvitationIsNotPending>(respondResult.value)
            }
        }
    }

    @Test
    fun `Can reject invitation`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val inviteeName = generateRandomString()
        val inviteeId = bootstrapUser(inviteeName)
        val channelsService = createChannelsService(testClock)
        val invitationId =
            when (val invitationResult = channelsService.inviteMember(channelId.toLong(), userId, inviteeName, MembershipRole.MEMBER)) {
                is Either.Left -> throw AssertionError("Invitation failed: ${invitationResult.value}")
                is Either.Right -> {
                    assertIs<Int>(invitationResult.value)
                    invitationResult.value
                }
            }
        when (val respondResult = channelsService.respondInvitation(inviteeId, invitationId.toLong(), false)) {
            is Either.Left -> throw AssertionError("Responding to invitation failed: ${respondResult.value}")
            is Either.Right -> {
                assertIs<Int>(respondResult.value)
            }
        }
        when (val respondResult = channelsService.respondInvitation(inviteeId, invitationId.toLong(), true)) {
            is Either.Right -> throw AssertionError(
                "Responding to invitation didn't fail after being already rejected: ${respondResult.value}",
            )
            is Either.Left -> {
                assertIs<RespondInvitationError.InvitationIsNotPending>(respondResult.value)
            }
        }
    }

    @Test
    fun `Can send Message`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)
        val message = generateRandomString()
        when (val messageResult = channelsService.sendMessage(channelId.toLong(), userId, message)) {
            is Either.Left -> throw AssertionError("Sending message failed: ${messageResult.value}")
            is Either.Right -> {
                assertIs<Int>(messageResult.value)
            }
        }
    }

    /*
    @Test
    fun `Message is received by other user`(){

    }
     */
    @Test
    fun `Can get Message`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)
        val message = generateRandomString()
        when (val messageResult = channelsService.sendMessage(channelId.toLong(), userId, message)) {
            is Either.Left -> throw AssertionError("Sending message failed: ${messageResult.value}")
            is Either.Right -> {
                assertIs<Int>(messageResult.value)
            }
        }
        when (val messagesResult = channelsService.getMessages(channelId.toLong(), userId)) {
            is Either.Left -> throw AssertionError("Getting messages failed: ${messagesResult.value}")
            is Either.Right -> {
                assertIs<List<MessageOutput>>(messagesResult.value)
                assertEquals(1, messagesResult.value.size)
                assertEquals(message, messagesResult.value[0].content)
            }
        }
    }

    @Test
    fun `Can get Multiple Messages`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)
        val message = generateRandomMessage()
        when (val messageResult = channelsService.sendMessage(channelId.toLong(), userId, message)) {
            is Either.Left -> throw AssertionError("Sending message failed: ${messageResult.value}")
            is Either.Right -> {
                assertIs<Int>(messageResult.value)
            }
        }
        val message2 = generateRandomMessage()
        when (val messageResult = channelsService.sendMessage(channelId.toLong(), userId, message2)) {
            is Either.Left -> throw AssertionError("Sending message failed: ${messageResult.value}")
            is Either.Right -> {
                assertIs<Int>(messageResult.value)
            }
        }
        val message3 = generateRandomMessage()
        when (val messageResult = channelsService.sendMessage(channelId.toLong(), userId, message3)) {
            is Either.Left -> throw AssertionError("Sending message failed: ${messageResult.value}")
            is Either.Right -> {
                assertIs<Int>(messageResult.value)
            }
        }
        when (val messagesResult = channelsService.getMessages(channelId.toLong(), userId)) {
            is Either.Left -> throw AssertionError("Getting messages failed: ${messagesResult.value}")
            is Either.Right -> {
                assertIs<List<MessageOutput>>(messagesResult.value)
                assertEquals(3, messagesResult.value.size)
                assertEquals(message, messagesResult.value[2].content)
                assertEquals(message2, messagesResult.value[1].content)
                assertEquals(message3, messagesResult.value[0].content)
            }
        }
    }

    @Test
    fun `Can Get Membership`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)
        when (val membershipResult = channelsService.getMembership(channelId.toLong(), userId)) {
            is Either.Left -> throw AssertionError("Getting membership failed: ${membershipResult.value}")
            is Either.Right -> {
                assertIs<MembershipOutput>(membershipResult.value)
                assertEquals(MembershipRole.OWNER, membershipResult.value.role)
            }
        }
    }

    @Test
    fun `Can get Multiple Memberships`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)
        val inviteeName = generateRandomString()
        val inviteeId = bootstrapUser(inviteeName)
        var invitationId = 0
        when (val invitationResult = channelsService.inviteMember(channelId.toLong(), userId, inviteeName, MembershipRole.MEMBER)) {
            is Either.Left -> throw AssertionError("Invitation failed: ${invitationResult.value}")
            is Either.Right -> {
                assertIs<Int>(invitationResult.value)
                invitationId = invitationResult.value
            }
        }

        when (val respondResult = channelsService.respondInvitation(inviteeId, invitationId.toLong(), true)) {
            is Either.Left -> throw AssertionError("Responding to invitation failed: ${respondResult.value}")
            is Either.Right -> {
                assertIs<Int>(respondResult.value)
            }
        }
        when (val membershipsResult = channelsService.getMemberships(channelId.toLong(), userId)) {
            is Either.Left -> throw AssertionError("Getting memberships failed: ${membershipsResult.value}")
            is Either.Right -> {
                assertIs<List<MembershipOutput>>(membershipsResult.value)
                assertEquals(2, membershipsResult.value.size)
                assertEquals(MembershipRole.OWNER, membershipsResult.value[0].role)
                assertEquals(MembershipRole.MEMBER, membershipsResult.value[1].role)
            }
        }
    }

    @Test
    fun `Can leave channel`() {
        val userId = bootstrapUser()
        val testClock = TestClock()
        val channelId = bootstrapChannel(userId)
        val channelsService = createChannelsService(testClock)
        val inviteeName = generateRandomString()
        val inviteeId = bootstrapUser(inviteeName)
        var invitationId = 0
        when (val invitationResult = channelsService.inviteMember(channelId.toLong(), userId, inviteeName, MembershipRole.MEMBER)) {
            is Either.Left -> throw AssertionError("Invitation failed: ${invitationResult.value}")
            is Either.Right -> {
                assertIs<Int>(invitationResult.value)
                invitationId = invitationResult.value
            }
        }

        when (val respondResult = channelsService.respondInvitation(inviteeId, invitationId.toLong(), true)) {
            is Either.Left -> throw AssertionError("Responding to invitation failed: ${respondResult.value}")
            is Either.Right -> {
                assertIs<Int>(respondResult.value)
            }
        }
        when (val leaveResult = channelsService.leaveChannel(channelId.toLong(), inviteeId)) {
            is Either.Left -> throw AssertionError("Leaving channel failed: ${leaveResult.value}")
            is Either.Right -> {
                assertIs<Unit>(leaveResult.value)
            }
        }
        when (val membershipsResult = channelsService.getMemberships(channelId.toLong(), userId)) {
            is Either.Left -> throw AssertionError("Getting memberships failed: ${membershipsResult.value}")
            is Either.Right -> {
                assertIs<List<MembershipOutput>>(membershipsResult.value)
                assertEquals(1, membershipsResult.value.size)
                assertEquals(MembershipRole.OWNER, membershipsResult.value[0].role)
            }
        }
    }

    companion object {
        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()

        private val transactionManager = JdbiTransactionManager(jdbi)

        private val channelDomain =
            ChannelDomain()

        private val userDomain =
            UserDomain(
                BCryptPasswordEncoder(),
                UUIDTokenEncoder(),
                UserDomainConfig(30.days, 30.minutes, 3, 24.hours),
            )

        private fun createChannelsService(testClock: TestClock) =
            ChannelService(
                transactionManager,
                channelDomain,
                testClock,
            )

        private fun bootstrapUser(username: String = generateRandomString()): Int {
            return transactionManager.run {
                return@run it.usersRepository.createUser(
                    username,
                    generateRandomEmail(),
                    userDomain.hashPassword("Password123@"),
                )
            }
        }

        private fun bootstrapChannel(
            ownerId: Int,
            isPublic: Boolean = true,
        ): Int {
            return transactionManager.run {
                return@run it.channelsRepository.createChannel(
                    generateRandomString(),
                    isPublic,
                    ownerId,
                )
            }
        }
    }
}
