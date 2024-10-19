package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.Token
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException
import java.util.UUID
import kotlin.jvm.Throws

class TokenMapper : ColumnMapper<Token> {
    @Throws(SQLException::class)
    override fun map(
        rs: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): Token = Token(UUID.fromString(rs.getString(columnNumber)))
}
