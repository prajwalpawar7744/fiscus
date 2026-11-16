package com.prajwalpawar.fiscus.model

/**
 * An account with its current balance (opening balance + all its transactions) and how many
 * transactions refer to it, transfers in included.
 */
data class AccountWithBalance(
    val account: Account,
    val balance: Money,
    val transactionCount: Int = 0,
)
