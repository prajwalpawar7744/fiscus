package com.prajwalpawar.fiscus.model

import kotlin.time.Instant

/**
 * A dated movement of money (DOMAIN.md). [amount] is always positive; the direction comes from
 * [type]. Transfers move money from [accountId] to [toAccountId] and have no category.
 */
data class Transaction(
    val id: Long = 0L,
    val type: TransactionType,
    val amount: Money,
    val accountId: Long,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val note: String = "",
    val occurredAt: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(amount.amountMinor > 0L) { "Amount must be positive" }
        when (type) {
            TransactionType.TRANSFER -> {
                require(toAccountId != null) { "A transfer needs a destination account" }
                require(toAccountId != accountId) { "A transfer needs two different accounts" }
                require(categoryId == null) { "A transfer has no category" }
            }
            TransactionType.INCOME, TransactionType.EXPENSE -> {
                require(toAccountId == null) { "Only transfers have a destination account" }
                require(categoryId != null) { "Income and expenses need a category" }
            }
        }
    }
}

enum class TransactionType { INCOME, EXPENSE, TRANSFER }
