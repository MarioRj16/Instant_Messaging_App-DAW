package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.domain.Channel
import com.example.messagingapp.domain.ChannelInvitation
import com.example.messagingapp.domain.Membership
import com.example.messagingapp.domain.Message
import com.example.messagingapp.repository.ChannelsRepository
import com.example.messagingapp.utils.PaginatedResponse
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
        clock: Clock,
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
                .bind("createdAt", clock.now().epochSeconds)
                .executeAndReturnGeneratedKeys("channel_id")
                .mapTo<Int>()
                .one()
        return channelId
    }

    override fun getChannel(
        channelId: Int,
        userId: Int,
    ): Channel? {
        val channel =
            handle
                .createQuery(
                    """
                SELECT *
                FROM channel c
                LEFT JOIN membership m ON c.channel_id = m.channel_id AND m.member_id = :userId
                JOIN users u ON c.owner_id = u.user_id
                WHERE c.channel_id = :channelId
                """,
                )
                .bind("channelId", channelId)
                .bind("userId", userId)
                .mapTo<Channel>()
                .singleOrNull() ?: return null

        val members = listChannelMembers(listOf(channelId))
        return channel.copy(members = members[channelId] ?: emptyList())
    }

    override fun listJoinedChannels(
        userId: Int,
        page: Int,
        pageSize: Int,
    ): PaginatedResponse<Channel> {
        val offset = calculateOffset(page, pageSize)
        val totalCount =
            handle
                .createQuery(
                    """
                SELECT COUNT(*) 
                FROM channel c
                JOIN membership m ON c.channel_id = m.channel_id
                WHERE m.member_id = :userId
                """,
                )
                .bind("userId", userId)
                .mapTo<Int>()
                .one()

        var channels =
            handle
                .createQuery(
                    """
                SELECT *
                FROM channel c
                JOIN membership m ON c.channel_id = m.channel_id
                JOIN users u ON c.owner_id = u.user_id
                WHERE m.member_id = :userId
                LIMIT :pageSize OFFSET :offset
                """,
                )
                .bind("userId", userId)
                .bind("pageSize", pageSize)
                .bind("offset", offset)
                .mapTo<Channel>()
                .list()

        val totalPages = calculateTotalPages(totalCount, pageSize)
        val channelIds = channels.map { it.channelId }
        val members = listChannelMembers(channelIds)
        channels = channels.map { c -> c.copy(members = members[c.channelId] ?: emptyList()) }
        return PaginatedResponse(channels, page, totalPages, totalCount)
    }

    override fun searchChannels(
        userId: Int,
        name: String,
        page: Int,
        pageSize: Int,
    ): PaginatedResponse<Channel> {
        val offset = calculateOffset(page, pageSize)

        val totalCount =
            handle
                .createQuery(
                    """
            SELECT COUNT(*) 
            FROM channel c
            LEFT JOIN membership m ON c.channel_id = m.channel_id
            JOIN users u ON c.owner_id = u.user_id
            WHERE c.channel_name ILIKE :name 
            AND (c.is_public = TRUE OR m.member_id = :userId)
            """,
                )
                .bind("name", "$name%")
                .bind("userId", userId)
                .mapTo<Int>()
                .one()

        var channels =
            handle
                .createQuery(
                    """
            SELECT *
            FROM channel c
            LEFT JOIN membership m ON c.channel_id = m.channel_id
            JOIN users u ON c.owner_id = u.user_id
            WHERE c.channel_name ILIKE :name 
            AND (c.is_public = TRUE OR m.member_id = :userId)
            LIMIT :pageSize OFFSET :offset
            """,
                )
                .bind("name", "$name%")
                .bind("userId", userId)
                .bind("pageSize", pageSize)
                .bind("offset", offset)
                .mapTo<Channel>()
                .list()

        val totalPages = calculateTotalPages(totalCount, pageSize)
        val channelIds = channels.map { it.channelId }
        val members = listChannelMembers(channelIds)
        channels = channels.map { c -> c.copy(members = members[c.channelId] ?: emptyList()) }

        return PaginatedResponse(channels, page, totalPages, totalCount)
    }

    override fun deleteChannel(channelId: Int) {
        handle
            .createUpdate("DELETE FROM channel WHERE channel_id = :channelId")
            .bind("channelId", channelId)
            .execute()
    }

    override fun listMessages(channelId: Int): List<Message> =
        handle
            .createQuery(
                """
            SELECT *
            FROM message m
            JOIN users u ON m.sender_id = u.user_id
            WHERE m.channel_id = :channelId
        """,
            )
            .bind("channelId", channelId)
            .mapTo<Message>()
            .list()

    override fun createMessage(
        channelId: Int,
        userId: Int,
        content: String,
        clock: Clock,
    ): Int =
        handle
            .createUpdate(
                "INSERT INTO message (channel_id, sender_id, created_at, content) VALUES (:channelId, :userId, :createdAt, :content)",
            )
            .bind("channelId", channelId)
            .bind("userId", userId)
            .bind("createdAt", clock.now().epochSeconds)
            .bind("content", content)
            .executeAndReturnGeneratedKeys("message_id")
            .mapTo<Int>()
            .one()

    override fun getMembership(
        channelId: Int,
        userId: Int,
    ): Membership? =
        handle
            .createQuery(
                """
                SELECT * 
                FROM membership 
                JOIN users u ON member_id = u.user_id
                WHERE channel_id = :channelId AND member_id = :userId
                """,
            )
            .bind("channelId", channelId)
            .bind("userId", userId)
            .mapTo<Membership>()
            .singleOrNull()

    override fun listMemberships(
        channelId: Int,
        page: Int,
        pageSize: Int,
    ): PaginatedResponse<Membership> {
        val offset = calculateOffset(page, pageSize)
        val totalCount =
            handle
                .createQuery(
                    """
                SELECT COUNT(*) 
                FROM membership m
                JOIN users u ON m.member_id = u.user_id
                WHERE m.channel_id = :channelId
                """,
                )
                .bind("channelId", channelId)
                .mapTo<Int>()
                .one()

        val memberships =
            handle
                .createQuery(
                    """
                SELECT *
                FROM membership m
                JOIN users u ON m.member_id = u.user_id
                WHERE m.channel_id = :channelId
                LIMIT :pageSize OFFSET :offset
                """,
                )
                .bind("channelId", channelId)
                .bind("pageSize", pageSize)
                .bind("offset", offset)
                .mapTo<Membership>()
                .list()

        val totalPages = calculateTotalPages(totalCount, pageSize)
        return PaginatedResponse(memberships, page, totalPages, totalCount)
    }

    override fun createChannelInvitation(
        channelId: Int,
        inviterId: Int,
        inviteeId: Int,
        role: String,
        clock: Clock,
    ): Int =
        handle
            .createUpdate(
                """INSERT INTO channel_invitation (inviter_id, invitee_id, channel_id, role, created_at) 
                    VALUES (:inviterId, :inviteeId, :channelId, :role::invite_role, :createdAt)""",
            )
            .bind("inviterId", inviterId)
            .bind("inviteeId", inviteeId)
            .bind("channelId", channelId)
            .bind("role", role)
            .bind("createdAt", clock.now().epochSeconds)
            .executeAndReturnGeneratedKeys("channel_invitation_id")
            .mapTo<Int>()
            .one()

    override fun listInvitations(
        userId: Int,
        page: Int,
        pageSize: Int,
    ): PaginatedResponse<ChannelInvitation> {
        val offset = calculateOffset(page, pageSize)

        val totalCount =
            handle
                .createQuery(
                    """
                SELECT COUNT(*) 
                FROM channel_invitation ci 
                WHERE ci.invitee_id = :userId
                """,
                )
                .bind("userId", userId)
                .mapTo<Int>()
                .one()

        val invitations =
            handle
                .createQuery(
                    """
                SELECT *
                FROM channel_invitation ci 
                JOIN users u ON ci.inviter_id = u.user_id
                JOIN channel c ON ci.channel_id = c.channel_id
                WHERE ci.invitee_id = :userId
                LIMIT :pageSize OFFSET :offset
                """,
                )
                .bind("userId", userId)
                .bind("pageSize", pageSize)
                .bind("offset", offset)
                .mapTo<ChannelInvitation>()
                .list()

        val totalPages = calculateTotalPages(totalCount, pageSize)
        return PaginatedResponse(invitations, page, totalPages, totalCount)
    }

    override fun getInvitation(invitationId: Int): ChannelInvitation? =
        handle
            .createQuery(
                """
                SELECT * 
                FROM channel_invitation ci 
                JOIN users u ON ci.inviter_id = u.user_id
                JOIN channel c ON ci.channel_id = c.channel_id
                WHERE ci.channel_invitation_id = :invitationId
                """,
            )
            .bind("invitationId", invitationId)
            .mapTo<ChannelInvitation>()
            .singleOrNull()

    override fun getInvitation(
        channelId: Int,
        userId: Int,
    ): ChannelInvitation? =
        handle
            .createQuery(
                """
                SELECT *
                FROM channel_invitation ci
                JOIN users u ON ci.inviter_id = u.user_id
                JOIN channel c ON ci.channel_id = c.channel_id
                WHERE ci.channel_id = :channelId AND ci.invitee_id = :userId
                """,
            )
            .bind("channelId", channelId)
            .bind("userId", userId)
            .mapTo<ChannelInvitation>()
            .singleOrNull()

    override fun deleteInvitation(invitationId: Int) {
        handle
            .createUpdate("DELETE FROM channel_invitation WHERE channel_invitation_id = :invitationId")
            .bind("invitationId", invitationId)
            .execute()
    }

    override fun deleteMembership(
        channelId: Int,
        userId: Int,
    ) {
        handle
            .createUpdate("DELETE FROM membership WHERE channel_id = :channelId AND member_id = :userId")
            .bind("channelId", channelId)
            .bind("userId", userId)
            .execute()
    }

    override fun createMembership(
        userId: Int,
        channelId: Int,
        clock: Clock,
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
            .bind("joinedAt", clock.now().epochSeconds)
            .execute()
    }

    private fun listChannelMembers(channelIds: List<Int>): Map<Int, List<Membership>> =
        if (channelIds.isEmpty()) {
            emptyMap()
        } else {
            handle
                .createQuery(
                    """
                    SELECT *
                    FROM users u
                    JOIN membership m ON u.user_id = m.member_id
                    WHERE m.channel_id IN (<channelIds>)
                    """.trimIndent(),
                )
                .bindList("channelIds", channelIds)
                .map { rs, ctx ->
                    val channelId = rs.getInt("channel_id")
                    val member = ctx.findRowMapperFor(Membership::class.java).get().map(rs, ctx)
                    channelId to member
                }
                .groupBy { it.first }
                .mapValues { it.value.map { (_, user) -> user } }
        }

    private fun calculateOffset(
        page: Int,
        pageSize: Int,
    ): Int = (page - 1) * pageSize

    private fun calculateTotalPages(
        totalCount: Int,
        pageSize: Int,
    ): Int = (totalCount + pageSize - 1) / pageSize
}
