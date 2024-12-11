package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.TestClock
import com.example.messagingapp.bootstrapChannel
import com.example.messagingapp.clearDatabase
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.domain.Password
import com.example.messagingapp.generateInvitationCode
import com.example.messagingapp.generateRandomString
import com.example.messagingapp.jdbi
import com.example.messagingapp.runWithHandle
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.minutes

class JdbiChannelRepositoryTests {
    @AfterEach
    fun tearDown() {
        clearDatabase(jdbi)
    }

    @Test
    fun `Channel can be created and retrieved`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = "channel"
            val userId = bootstrapUser(userRepo)

            val channelId = repo.createChannel(channelName, true, userId, TestClock())

            val channel = repo.getChannel(channelId, userId) ?: throw AssertionError("Channel $channelId was not found")

            assertEquals(channelId, channel.channelId)
            assertEquals(channelName, channel.channelName)
            assertEquals(userId, channel.owner.userId)
            assertTrue(channel.isPublic)
        }
    }

    @Test
    fun `Joined channels can be retrieved`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)

            val channelName1 = "channel"
            val channelName2 = "channel2"
            val channelName3 = "channel3"

            val clock = TestClock()
            val userId = bootstrapUser(userRepo)
            val userId2 = bootstrapUser(userRepo)

            val channelId1 = repo.createChannel(channelName1, true, userId, clock)
            repo.createMembership(userId, channelId1, clock, MembershipRole.OWNER.role)

            val channelId2 = repo.createChannel(channelName2, false, userId, clock)
            repo.createMembership(userId, channelId2, clock, MembershipRole.OWNER.role)

            val channelId3 = repo.createChannel(channelName3, true, userId2, clock)
            repo.createMembership(userId2, channelId3, clock, MembershipRole.OWNER.role)

            val search = repo.listJoinedChannels(userId)
            assertEquals(2, search.totalSize)
        }
    }

    @Test
    fun `Channels can be searched`() {
        runWithHandle {
            val channelsRepository = JdbiChannelsRepository(it)
            val usersRepository = JdbiUsersRepository(it)
            val channelName1 = "channel"
            val channelName2 = "channel2"
            val channelName3 = "channel3"
            val clock = TestClock()
            val userId = bootstrapUser(usersRepository)
            val userId2 = bootstrapUser(usersRepository)

            val channelId = channelsRepository.createChannel(channelName1, true, userId, clock)
            channelsRepository.createMembership(userId, channelId, clock, MembershipRole.OWNER.role)
            channelsRepository.createMembership(userId2, channelId, clock, MembershipRole.MEMBER.role)
            val channelId2 = channelsRepository.createChannel(channelName2, false, userId, clock)
            channelsRepository.createMembership(userId, channelId2, clock, MembershipRole.OWNER.role)
            val channelId3 = channelsRepository.createChannel(channelName3, false, userId2, clock)
            channelsRepository.createMembership(userId2, channelId3, clock, MembershipRole.OWNER.role)

            val search = channelsRepository.searchChannels(userId, "CHANNEL")
            assertEquals(2, search.totalSize)

            val search2 = channelsRepository.searchChannels(userId, "channel3")
            assertEquals(0, search2.totalSize)
        }
    }

    @Test
    fun `Channel can be deleted`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = "channel"
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId, TestClock())

            val channel = repo.getChannel(channelId, userId)
            assertNotNull(channel)

            repo.deleteChannel(channelId)

            val channel2 = repo.getChannel(channelId, userId)
            assertNull(channel2)
        }
    }

    @Test
    fun `Messages can be created and listed`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = "channel"
            val userId = bootstrapUser(userRepo)
            val channelId = bootstrapChannel(userId, channelName)
            val content = "messageContent"
            repo.createMessage(channelId, userId, content, TestClock())

            val messages = repo.listMessages(channelId)

            assertEquals(1, messages.size)
            assertEquals(content, messages[0].content)
            assertEquals(userId, messages[0].sender.userId)
        }
    }

    @Test
    fun `Messages are sorted by timestamp`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = "channel"
            val userId = bootstrapUser(userRepo)
            val channelId = bootstrapChannel(userId, channelName)
            val content = "messageContent"
            val clock = TestClock()
            repo.createMessage(channelId, userId, content, clock)
            clock.advance(1.minutes)
            repo.createMessage(channelId, userId, content, clock)
            clock.advance(1.minutes)
            repo.createMessage(channelId, userId, content, clock)

            val messages = repo.listMessages(channelId)

            assertEquals(3, messages.size)
            assertEquals(clock.now().minus(2.minutes), messages[0].createdAt)
            assertEquals(clock.now().minus(1.minutes), messages[1].createdAt)
            assertEquals(clock.now(), messages[2].createdAt)
        }
    }

    @Test
    fun `Membership can be created and retrieved`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = "channel"
            val userId = bootstrapUser(userRepo)

            val channelId = repo.createChannel(channelName, true, userId, TestClock())
            repo.createMembership(userId, channelId, TestClock(), MembershipRole.OWNER.role)

            val membership = repo.getMembership(channelId, userId) ?: throw AssertionError("Membership not found")

            assertNotNull(membership)
            assertEquals(userId, membership.member.userId)
            assertEquals(channelId, membership.channelId)
            assertEquals(MembershipRole.OWNER, membership.role)
        }
    }

    @Test
    fun `Memberships can be listed`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = "channel"
            val clock = TestClock()

            val userId = bootstrapUser(userRepo)
            val userId2 = bootstrapUser(userRepo)

            val channelId = repo.createChannel(channelName, true, userId, clock)
            repo.createMembership(userId, channelId, clock, MembershipRole.OWNER.role)
            repo.createMembership(userId2, channelId, clock, MembershipRole.VIEWER.role)

            val membership = repo.listMemberships(channelId)

            assertEquals(2, membership.totalSize)
        }
    }

    @Test
    fun `Membership can be deleted`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val clock = TestClock()
            val channelId = repo.createChannel(channelName, true, userId, clock)
            repo.createMembership(userId, channelId, clock, MembershipRole.MEMBER.role)
            val membership = repo.getMembership(channelId, userId)

            assertNotNull(membership)

            repo.deleteMembership(channelId, userId)

            val membership2 = repo.getMembership(channelId, userId)

            assertNull(membership2)
        }
    }

    @Test
    fun `Channel invitation can be created and retrieved`() {
        runWithHandle { handle ->
            val repo = JdbiChannelsRepository(handle)
            val userRepo = JdbiUsersRepository(handle)
            val channelName = "channel"

            val userId = bootstrapUser(userRepo)
            val userId2 = bootstrapUser(userRepo)

            val channelId = repo.createChannel(channelName, true, userId, TestClock())

            val invite =
                repo.createChannelInvitation(
                    channelId,
                    userId,
                    userId2,
                    MembershipRole.MEMBER.role,
                    TestClock(),
                )

            val invitation = repo.getInvitation(invite) ?: throw AssertionError("Invitation not found")

            assertEquals(userId, invitation.inviter.userId)
            assertEquals(userId2, invitation.inviteeId)
            assertEquals(channelId, invitation.channel.channelId)
        }
    }

    @Test
    fun `Channel invitations can be listed`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = "channel"
            val userId = bootstrapUser(userRepo)
            val userId2 = bootstrapUser(userRepo)

            val numberOfInvitations = 3
            repeat(numberOfInvitations) { num ->
                val channelId = bootstrapChannel(userId, "$channelName$num")
                repo.createChannelInvitation(
                    channelId,
                    userId,
                    userId2,
                    MembershipRole.MEMBER.role,
                    TestClock(),
                )
            }

            val invitations = repo.listInvitations(userId2)
            assertEquals(numberOfInvitations, invitations.totalSize)
        }
    }

    @Test
    fun `Channel invitation can be deleted`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = "channel"
            val userId = bootstrapUser(userRepo)
            val userId2 = bootstrapUser(userRepo)

            val channelId = bootstrapChannel(userId, channelName)
            val invite =
                repo.createChannelInvitation(
                    channelId,
                    userId,
                    userId2,
                    MembershipRole.MEMBER.role,
                    TestClock(),
                )

            val invitation = repo.getInvitation(invite) ?: throw AssertionError("Invitation not found")

            assertNotNull(invitation)
            repo.deleteInvitation(invite)

            val invitation2 = repo.getInvitation(invite)

            assertNull(invitation2)
        }
    }

    private fun bootstrapUser(userRepo: JdbiUsersRepository): Int {
        val userName = generateRandomString()
        val password = Password("Hash12345@")
        val invitationCode = generateInvitationCode(TestClock())
        return userRepo.createUser(userName, password, invitationCode)
    }
}
