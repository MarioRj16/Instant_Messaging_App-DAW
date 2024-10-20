package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.MembershipRole
import com.example.messagingapp.http.model.output.MembershipOutputModel
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class MembershipOutputMapper : RowMapper<MembershipOutputModel> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): MembershipOutputModel {
        return MembershipOutputModel(
            rs.getInt("membership_id"),
            rs.getInt("member_id"),
            rs.getInt("channel_id"),
            MembershipRole.fromRole(rs.getString("role")),
            Instant.fromEpochSeconds(rs.getLong("joined_at")).toString(),
        )
    }
}
