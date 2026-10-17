package com.prajwalpawar.fiscus.data.model

// An account with its current balance, in hundredths.
data class AccountBalance(
    val id: Long,
    val name: String,
    val type: AccountType,
    val isDefault: Boolean,
    val isArchived: Boolean,
    val balance: Long,
    // How many transactions use this account.
    val usageCount: Int
)
