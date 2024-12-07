package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.RegistrationInvitation
import kotlinx.datetime.Instant
import org.jdbi.v3.core.mapper.RowMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet

class RegistrationInvitationMapper : RowMapper<RegistrationInvitation> {
    override fun map(
        rs: ResultSet,
        ctx: StatementContext,
    ): RegistrationInvitation =
        RegistrationInvitation(
            rs.getString("invitation_code"),
            ctx.findColumnMapperFor(Instant::class.java).get().map(rs, "created_at", ctx),
        )
}
