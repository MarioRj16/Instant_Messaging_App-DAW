package com.example.messagingapp.repository

interface TransactionManager {
    fun <R> run(block: (transaction: Transaction) -> R): R
}
