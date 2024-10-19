package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.Environment
import com.example.messagingapp.domain.Password
import com.example.messagingapp.generateRandomEmail
import com.example.messagingapp.generateRandomMessage
import com.example.messagingapp.generateRandomString
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.postgresql.ds.PGSimpleDataSource
import kotlin.test.assertTrue

class JdbiChannelRepositoryTests {
    @Test
    fun `create Channel`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)

            val channelId = repo.createChannel(channelName, true, userId)
            assertNotNull(channelId)
        }
    }

    @Test
    fun `get Channel`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)

            val channelId = repo.createChannel(channelName, true, userId)
            val channel = repo.getChannel(channelId.toLong(), userId)
            assertNotNull(channel)
            assertEquals(channelName, channel!!.channelName)
            assertEquals(userId, channel.ownerId)
        }
    }

    @Test
    fun `get Joined Channels`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName1 = generateRandomString()
            val channelName2 = generateRandomString()
            val channelName3 = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val otherUser = bootstrapUser(userRepo)
            val channelId1 = repo.createChannel(channelName1, true, userId)
            val channelId2 = repo.createChannel(channelName2, true, userId)
            val channelId3 = repo.createChannel(channelName3, true, otherUser)
            val search = repo.getJoinedChannels(userId)
            assertEquals(2, search.size)
            assertEquals(channelName1, search[0].channelName)
            assertEquals(channelName2, search[1].channelName)
            assertEquals(channelId1, search[0].channelId)
            assertEquals(channelId2, search[1].channelId)
        }
    }

    @Test
    fun `search Channels`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName1 = generateRandomString()
            val channelName2 = generateRandomString()
            val channelName3 = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val otherUser = bootstrapUser(userRepo)
            val channelId1 = repo.createChannel(channelName1, true, userId)
            val channelId2 = repo.createChannel(channelName2, false, otherUser)
            val channelId3 = repo.createChannel(channelName3, true, otherUser)
            val search = repo.searchChannels()
            for (channel in search) {
                assertEquals(true, channel.isPublic)
            }
        }
    }

    @Test
    fun `join Channel`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val otherUser = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            repo.joinChannel(channelId.toLong(), otherUser)
            val membership = repo.getMembership(channelId.toLong(), otherUser)
            assertNotNull(membership)
            assertEquals(otherUser, membership!!.userId)
            assertEquals(channelId, membership.channelId)
        }
    }

    @Test
    fun `get Messages`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val messages = repo.getMessages(channelId.toLong())
            assertNotNull(messages)
            val content = generateRandomMessage()
            val message = repo.sendMessage(channelId.toLong(), userId, content)
            val messages2 = repo.getMessages(channelId.toLong())
            assertEquals(1, messages2.size)
            assertEquals(content, messages2[0].content)
        }
    }

    @Test
    fun `send Message`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val content = generateRandomMessage()
            val message = repo.sendMessage(channelId.toLong(), userId, content)
            assertNotNull(message)
            assertTrue { message >= 1 }
        }
    }

    @Test
    fun `get Membership`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val membership = repo.getMembership(channelId.toLong(), userId)
            assertNotNull(membership)
            assertEquals(userId, membership!!.userId)
            assertEquals(channelId, membership.channelId)
        }
    }

    @Test
    fun `get Memberships`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val memberships = repo.getMemberships(channelId.toLong())
            assertNotNull(memberships)
            assertEquals(1, memberships.size)
            assertEquals(userId, memberships[0].userId)
            assertEquals(channelId, memberships[0].channelId)
            val otherUser = bootstrapUser(userRepo)
            repo.inviteMember(channelId.toLong(), userId, otherUser, "member", Long.MAX_VALUE)
            repo.acceptInvitation(1, otherUser, channelId.toInt(), "member")
            val memberships2 = repo.getMemberships(channelId.toLong())
            assertEquals(2, memberships2.size)
        }
    }

    @Test
    fun `invite Member`() {
        runWithHandle { handle ->
            val repo = JdbiChannelsRepository(handle)
            val userRepo = JdbiUsersRepository(handle)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val otherUser = bootstrapUser(userRepo)
            val invite = repo.inviteMember(channelId.toLong(), userId, otherUser, "member", Long.MAX_VALUE)
            assertNotNull(invite)
        }
    }

    @Test
    fun `get Invitations`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val otherUser = bootstrapUser(userRepo)
            repo.inviteMember(channelId.toLong(), userId, otherUser, "member", Long.MAX_VALUE)
            val invitations = repo.getInvitations(otherUser)
            assertNotNull(invitations)
            assertEquals(1, invitations.size)
            assertEquals(userId, invitations[0].inviterId)
            assertEquals(otherUser, invitations[0].inviteeId)
            assertEquals(channelId, invitations[0].channelId)
        }
    }

    @Test
    fun `get Invitation`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val otherUser = bootstrapUser(userRepo)
            val invite = repo.inviteMember(channelId.toLong(), userId, otherUser, "member", Long.MAX_VALUE)
            val invitation = repo.getInvitation(userId, otherUser, channelId.toInt())
            assertNotNull(invitation)
            assertEquals(userId, invitation!!.inviterId)
            assertEquals(otherUser, invitation.inviteeId)
            assertEquals(channelId, invitation.channelId)
        }
    }

    @Test
    fun `get Invitation By Id`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val otherUser = bootstrapUser(userRepo)
            val invite = repo.inviteMember(channelId.toLong(), userId, otherUser, "member", Long.MAX_VALUE)
            val invitation = repo.getInvitationById(invite.toLong())
            assertNotNull(invitation)
            assertEquals(userId, invitation!!.inviterId)
            assertEquals(otherUser, invitation.inviteeId)
            assertEquals(channelId, invitation.channelId)
        }
    }

    @Test
    fun `accept Invitation`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val otherUser = bootstrapUser(userRepo)
            val invite = repo.inviteMember(channelId.toLong(), userId, otherUser, "member", Long.MAX_VALUE)
            val invitation = repo.getInvitation(userId, otherUser, channelId.toInt())
            val accept = repo.acceptInvitation(invitation!!.channelInvitationId.toLong(), otherUser, channelId.toInt(), "member")
            assertNotNull(accept)
            val invitation2 = repo.getInvitation(userId, otherUser, channelId.toInt())
            assertEquals("accepted", invitation2!!.status.status)
        }
    }

    @Test
    fun `decline Invitation`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val otherUser = bootstrapUser(userRepo)
            repo.inviteMember(channelId.toLong(), userId, otherUser, "member", Long.MAX_VALUE)
            val invitation = repo.getInvitation(userId, otherUser, channelId.toInt())
            val decline = repo.declineInvitation(invitation!!.channelInvitationId.toLong())
            assertNotNull(decline)
            val invitation2 = repo.getInvitation(userId, otherUser, channelId.toInt())
            assertEquals("rejected", invitation2!!.status.status)
        }
    }

    @Test
    fun `leave Channel`() {
        runWithHandle {
            val repo = JdbiChannelsRepository(it)
            val userRepo = JdbiUsersRepository(it)
            val channelName = generateRandomString()
            val userId = bootstrapUser(userRepo)
            val channelId = repo.createChannel(channelName, true, userId)
            val membership = repo.getMembership(channelId.toLong(), userId)
            assertNotNull(membership)
            repo.leaveChannel(channelId.toLong(), userId)
            val membership2 = repo.getMembership(channelId.toLong(), userId)
            assertEquals(null, membership2)
        }
    }

    companion object {
        private fun runWithHandle(block: (Handle) -> Unit) = jdbi.useTransaction<Exception>(block)

        private fun bootstrapUser(userRepo: JdbiUsersRepository): Int {
            val userName = generateRandomString()
            val email = generateRandomEmail()
            val password = Password("Hash12345@")
            return userRepo.createUser(userName, email, password)
        }

        private val jdbi =
            Jdbi
                .create(
                    PGSimpleDataSource().apply {
                        setURL(Environment.getDbUrl())
                    },
                ).configureWithAppRequirements()
    }
}
