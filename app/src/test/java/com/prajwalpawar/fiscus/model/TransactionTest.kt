package com.prajwalpawar.fiscus.model

import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.time.Instant

class TransactionTest {

    private val now = Instant.fromEpochMilliseconds(1_700_000_000_000)

    private fun transaction(
        type: TransactionType,
        amountMinor: Long = 100,
        toAccountId: Long? = null,
        categoryId: Long? = null,
    ) = Transaction(
        type = type,
        amount = Money(amountMinor, "INR"),
        accountId = 1,
        toAccountId = toAccountId,
        categoryId = categoryId,
        occurredAt = now,
        createdAt = now,
        updatedAt = now,
    )

    @Test
    fun `valid expense, income and transfer are accepted`() {
        transaction(TransactionType.EXPENSE, categoryId = 1)
        transaction(TransactionType.INCOME, categoryId = 1)
        transaction(TransactionType.TRANSFER, toAccountId = 2)
    }

    @Test
    fun `amount must be positive`() {
        assertThrows(IllegalArgumentException::class.java) {
            transaction(TransactionType.EXPENSE, amountMinor = 0, categoryId = 1)
        }
    }

    @Test
    fun `expense needs a category and no destination`() {
        assertThrows(IllegalArgumentException::class.java) { transaction(TransactionType.EXPENSE) }
        assertThrows(IllegalArgumentException::class.java) {
            transaction(TransactionType.EXPENSE, categoryId = 1, toAccountId = 2)
        }
    }

    @Test
    fun `transfer needs a different destination and no category`() {
        assertThrows(IllegalArgumentException::class.java) { transaction(TransactionType.TRANSFER) }
        assertThrows(IllegalArgumentException::class.java) {
            transaction(TransactionType.TRANSFER, toAccountId = 1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            transaction(TransactionType.TRANSFER, toAccountId = 2, categoryId = 1)
        }
    }
}
