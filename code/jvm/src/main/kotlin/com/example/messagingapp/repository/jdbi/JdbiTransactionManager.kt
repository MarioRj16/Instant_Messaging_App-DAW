package com.example.messagingapp.repository.jdbi

import com.example.messagingapp.repository.Transaction
import com.example.messagingapp.repository.TransactionManager
import org.jdbi.v3.core.Jdbi
import org.springframework.stereotype.Component

@Component
class JdbiTransactionManager(
    private val jdbi: Jdbi,
) : TransactionManager {
    override fun <R> run(block: (Transaction) -> R): R =
        jdbi.inTransaction<R, Exception> { handle ->
            block(JdbiTransaction(handle))
        }
}
