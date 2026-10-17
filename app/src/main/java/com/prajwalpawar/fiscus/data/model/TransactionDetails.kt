package com.prajwalpawar.fiscus.data.model

// A transaction with the names needed to display it.
data class TransactionDetails(
    val id: Long,
    val type: TransactionType,
    val amount: Long,
    val title: String,
    val note: String?,
    val date: Long,
    val categoryName: String?,
    val categoryIconKey: String?,
    val accountName: String?,
    val sourceAccountName: String?,
    val destinationAccountName: String?
)
