package com.prajwalpawar.fiscus.data.model

data class Transaction (
    val id: Long = 0L,
    val type: TransactionType,
    val amount: Long,
    val categoryId: Long? = null,
    val accountId: Long? = null,
    val sourceAccountId: Long? = null,
    val destinationAccountId: Long? = null,
    val note: String? = null,
    val date: Long
)