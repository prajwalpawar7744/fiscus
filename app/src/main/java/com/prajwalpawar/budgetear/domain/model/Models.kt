package com.prajwalpawar.budgetear.domain.model

import java.util.Date

enum class TransactionType {
    INCOME, EXPENSE
}

data class Transaction(
    val id: Long? = null,
    val title: String,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val accountId: Long,
    val date: Date,
    val note: String = ""
)

data class Category(
    val id: Long? = null,
    val name: String,
    val icon: String,
    val color: Int
)

data class Account(
    val id: Long? = null,
    val name: String,
    val balance: Double,
    val icon: String
)
