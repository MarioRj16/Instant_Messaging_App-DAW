package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.Membership
import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.domain.User
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class MembershipMapper : RowMapper<Membership> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): Membership {
        return Membership(
            rs.getInt("membership_id"),
            ctx.findRowMapperFor(User::class.java).get().map(rs, ctx),
            rs.getInt("channel_id"),
            MembershipRole.fromRole(rs.getString("role")),
            ctx.findColumnMapperFor(Instant::class.java).get().map(rs, "joined_at", ctx),
        )
    }
}
