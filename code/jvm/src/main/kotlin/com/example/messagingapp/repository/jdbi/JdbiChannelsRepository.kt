package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.http.model.output.ChannelInvitationOutputModel
import com.example.messagingapp.http.model.output.ChannelWithMembershipOutputModel
import com.example.messagingapp.http.model.output.MembershipOutputModel
import com.example.messagingapp.http.model.output.MessageOutputModel
import com.example.messagingapp.repository.ChannelsRepository
import kotlinx.datetime.Clock
import org.jdbi.v3.core.Handle
import org.jdbi.v3.core.kotlin.mapTo

class JdbiChannelsRepository(
    private val handle: Handle,
) : ChannelsRepository {
    override fun createChannel(
        channelName: String,
        isPublic: Boolean,
        ownerId: Int,
    ): Int {
        val channelId =
            handle
                .createUpdate(
                    """
            INSERT INTO channel (channel_name, is_public, owner_id, created_at) 
            VALUES (:channelName, :isPublic, :ownerId, :createdAt)
            """,
                )
                .bind("channelName", channelName)
                .bind("isPublic", isPublic)
                .bind("ownerId", ownerId)
                .bind("createdAt", Clock.System.now().epochSeconds)
                .executeAndReturnGeneratedKeys("channel_id")
                .mapTo<Int>()
                .one()

        createMembership(ownerId, channelId, "owner")

        return channelId
    }

    override fun getChannel(
        channelId: Long,
        userId: Int,
    ): ChannelWithMembershipOutputModel? =
        handle
            .createQuery(
                """
                SELECT c.channel_id, c.owner_id, c.channel_name, c.created_at, c.is_public,
                       CASE WHEN m.member_id IS NOT NULL THEN TRUE ELSE FALSE END AS is_member
                FROM channel c
                LEFT JOIN membership m ON c.channel_id = m.channel_id AND m.member_id = :userId
                WHERE c.channel_id = :channelId
        """,
            )
            .bind("channelId", channelId)
            .bind("userId", userId)
            .mapTo<ChannelWithMembershipOutputModel>()
            .singleOrNull()

    override fun getJoinedChannels(userId: Int): List<ChannelWithMembershipOutputModel> =
        handle
            .createQuery(
                """
                SELECT c.channel_id, c.owner_id, c.channel_name, c.created_at, c.is_public,
                       CASE WHEN m.member_id IS NOT NULL THEN TRUE ELSE FALSE END AS is_member
                FROM channel c
                JOIN membership m ON c.channel_id = m.channel_id
                WHERE m.member_id = :userId
        """,
            )
            .bind("userId", userId)
            .mapTo<ChannelWithMembershipOutputModel>()
            .list()

    override fun searchChannels(): List<ChannelWithMembershipOutputModel> =
        handle
            .createQuery(
                """
                SELECT c.channel_id, c.owner_id, c.channel_name, c.created_at, c.is_public,
                       CASE WHEN m.member_id IS NOT NULL THEN TRUE ELSE FALSE END AS is_member
                FROM channel c
                LEFT JOIN membership m ON c.channel_id = m.channel_id
                WHERE c.is_public = TRUE
        """,
            )
            .mapTo<ChannelWithMembershipOutputModel>()
            .list()

    override fun joinChannel(
        channelId: Long,
        userId: Int,
    ): Unit = createMembership(userId, channelId.toInt(), "member")

    override fun getMessages(channelId: Long): List<MessageOutputModel> =
        handle
            .createQuery(
                """
            SELECT m.message_id, m.channel_id, m.sender_id, m.created_at, m.content, u.username AS sender_name
            FROM message m
            JOIN users u ON m.sender_id = u.user_id
            WHERE m.channel_id = :channelId
        """,
            )
            .bind("channelId", channelId)
            .mapTo<MessageOutputModel>()
            .list()

    override fun sendMessage(
        channelId: Long,
        userId: Int,
        content: String,
    ): Int =
        handle
            .createUpdate(
                "INSERT INTO message (channel_id, sender_id, created_at, content) VALUES (:channelId, :userId, :createdAt, :content)",
            )
            .bind("channelId", channelId)
            .bind("userId", userId)
            .bind("createdAt", Clock.System.now().epochSeconds)
            .bind("content", content)
            .executeAndReturnGeneratedKeys("message_id")
            .mapTo<Int>()
            .one()

    override fun getMembership(
        channelId: Long,
        userId: Int,
    ): MembershipOutputModel? =
        handle
            .createQuery("SELECT * FROM membership WHERE channel_id = :channelId AND member_id = :userId")
            .bind("channelId", channelId)
            .bind("userId", userId)
            .mapTo<MembershipOutputModel>()
            .singleOrNull()

    override fun getMemberships(channelId: Long): List<MembershipOutputModel> =
        handle
            .createQuery("SELECT * FROM membership WHERE channel_id = :channelId")
            .bind("channelId", channelId)
            .mapTo<MembershipOutputModel>()
            .list()

    override fun inviteMember(
        channelId: Long,
        userId: Int,
        invitedUserId: Int,
        role: String,
        expiresAt: Long,
    ): Int =
        handle
            .createUpdate(
                """INSERT INTO channel_invitation (inviter_id, invitee_id, channel_id, role, created_at, expires_at) 
                    VALUES (:userId, :invitedUserId, :channelId, :role::invite_role, :createdAt, :expiresAt)""",
            )
            .bind("userId", userId)
            .bind("invitedUserId", invitedUserId)
            .bind("channelId", channelId)
            .bind("role", role)
            .bind("createdAt", Clock.System.now().epochSeconds)
            .bind("expiresAt", expiresAt)
            .executeAndReturnGeneratedKeys("channel_invitation_id")
            .mapTo<Int>()
            .one()

    override fun getInvitations(userId: Int): List<ChannelInvitationOutputModel> =
        handle
            .createQuery(
                "SELECT * FROM channel_invitation WHERE invitee_id = :userId",
            )
            .bind("userId", userId)
            .mapTo<ChannelInvitationOutputModel>()
            .list()

    override fun getInvitation(
        inviterId: Int,
        inviteeId: Int,
        channelId: Int,
    ): ChannelInvitationOutputModel =
        handle
            .createQuery(
                "SELECT * FROM channel_invitation WHERE inviter_id = :inviterId AND invitee_id = :inviteeId AND channel_id = :channelId",
            )
            .bind("inviterId", inviterId)
            .bind("inviteeId", inviteeId)
            .bind("channelId", channelId)
            .mapTo<ChannelInvitationOutputModel>()
            .one()

    override fun getInvitationById(invitationId: Long): ChannelInvitationOutputModel? =
        handle
            .createQuery("SELECT * FROM channel_invitation WHERE channel_invitation_id = :invitationId")
            .bind("invitationId", invitationId)
            .mapTo<ChannelInvitationOutputModel>()
            .singleOrNull()

    /*
    override fun getPendingInvitationById(invitationId: Long): ChannelInvitationOutput =
        handle
            .createQuery("SELECT * FROM channel_invitation WHERE channel_invitation_id = :invitationId AND status = 'pending'::invite_status")
            .bind("invitationId",invitationId)
            .mapTo<ChannelInvitationOutput>()
            .one()

     */

    override fun acceptInvitation(
        invitationId: Long,
        userId: Int,
        channelId: Int,
        role: String,
    ): Int {
        val updateInvitation =
            handle
                .createUpdate("UPDATE channel_invitation SET status = 'accepted' WHERE channel_invitation_id = :invitationId ")
                .bind("invitationId", invitationId)
                .execute()

        createMembership(userId, channelId, role)

        return updateInvitation
    }

    override fun declineInvitation(invitationId: Long): Int =
        handle
            .createUpdate("UPDATE channel_invitation SET status = 'rejected' WHERE channel_invitation_id = :invitationId ")
            .bind("invitationId", invitationId)
            .execute()

    override fun leaveChannel(
        channelId: Long,
        userId: Int,
    ) {
        handle
            .createUpdate("DELETE FROM membership WHERE channel_id = :channelId AND member_id = :userId")
            .bind("channelId", channelId)
            .bind("userId", userId)
            .execute()
    }
    /*
      override fun kickMembers(channelId: Long, usersId: List<Int>): Boolean? {
        TODO("Not yet implemented")
    }
     */

    private fun createMembership(
        userId: Int,
        channelId: Int,
        role: String,
    ) {
        handle
            .createUpdate(
                """INSERT INTO membership (member_id, channel_id, role, joined_at) 
                    VALUES (:userId, :channelId, :role::membership_role, :joinedAt)""",
            )
            .bind("userId", userId)
            .bind("channelId", channelId)
            .bind("role", role)
            .bind("joinedAt", Clock.System.now().epochSeconds)
            .execute()
    }
}
