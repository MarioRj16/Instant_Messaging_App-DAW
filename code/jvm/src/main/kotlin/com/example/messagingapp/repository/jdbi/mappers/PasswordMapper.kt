package com.example.messagingapp.repository.jdbi.mappers

import com.example.messagingapp.domain.Password
import org.jdbi.v3.core.mapper.ColumnMapper
import org.jdbi.v3.core.statement.StatementContext
import java.sql.ResultSet
import java.sql.SQLException
import kotlin.jvm.Throws

class PasswordMapper : ColumnMapper<Password> {
    @Throws(SQLException::class)
    override fun map(
        r: ResultSet,
        columnNumber: Int,
        ctx: StatementContext,
    ): Password = Password(r.getString(columnNumber))
}
