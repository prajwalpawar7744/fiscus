package com.prajwalpawar.fiscus.model

/** Where money lives (DOMAIN.md). Each account has one currency (ADR-016). */
data class Account(
    val id: Long = 0L,
    val name: String,
    val type: AccountType,
    val currencyCode: String,
    val openingBalance: Money,
    val archived: Boolean = false,
) {
    init {
        require(name.isNotBlank()) { "Account name must not be blank" }
        require(openingBalance.currencyCode == currencyCode) { "Opening balance currency must match" }
    }
}

enum class AccountType { CASH, BANK, CARD, WALLET }
