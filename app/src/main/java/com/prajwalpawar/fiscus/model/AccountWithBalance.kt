package com.prajwalpawar.fiscus.model

/** An account with its current balance (opening balance + all its transactions). */
data class AccountWithBalance(val account: Account, val balance: Money)
