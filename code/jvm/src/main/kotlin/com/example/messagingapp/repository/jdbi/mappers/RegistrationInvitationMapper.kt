package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.InviteStatus
import com.example.messagingapp.domain.RegistrationInvitation
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.util.UUID

class RegistrationInvitationMapper : RowMapper<RegistrationInvitation> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): RegistrationInvitation =
        RegistrationInvitation(
            UUID.fromString(rs.getString("invitation_token")),
            rs.getInt("inviter_id"),
            Instant.fromEpochSeconds(rs.getLong("created_at")),
            InviteStatus.fromValue(rs.getString("status")),
        )
}
