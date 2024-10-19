package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.InviteStatus
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.jvm.Throws

class InvitationStatusMapper : ColumnMapper<InviteStatus> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext?,
    ): InviteStatus {
        val status = r.getString(columnNumber)
        return InviteStatus.fromValue(status)
    }
}
