package com.prajwalpawar.fiscus.data.model

data class Transaction (
    val id: Long = 0L,
    val type: TransactionType,
    val amount: Long,
    val category: String,
    val account: String,
    val note: String? = null,
    val date: Long
)