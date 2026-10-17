package com.prajwalpawar.fiscus.data.model

// An account with its current balance, in hundredths.
data class AccountBalance(
    val id: Long,
    val name: String,
    val type: AccountType,
    val balance: Long
)
