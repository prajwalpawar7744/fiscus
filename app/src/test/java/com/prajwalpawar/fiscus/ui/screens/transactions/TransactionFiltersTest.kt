package com.prajwalpawar.fiscus.ui.screens.transactions

import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.model.TransactionType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionFiltersTest {
    private fun details(
        type: TransactionType,
        categoryId: Long? = null,
        accountId: Long? = null,
        from: Long? = null,
        to: Long? = null
    ) = TransactionDetails(
        id = 1, type = type, amount = 100, title = "t", note = null, date = 0,
        categoryId = categoryId, accountId = accountId,
        sourceAccountId = from, destinationAccountId = to,
        categoryName = null, categoryIconKey = null, accountName = null,
        sourceAccountName = null, destinationAccountName = null
    )

    private val lunch = details(TransactionType.EXPENSE, categoryId = 5, accountId = 1)
    private val transfer = details(TransactionType.TRANSFER, from = 1, to = 2)

    @Test
    fun noFiltersMatchEverything() {
        assertTrue(lunch.matches(TransactionFilters()))
        assertTrue(transfer.matches(TransactionFilters()))
    }

    @Test
    fun typeFilter() {
        assertTrue(lunch.matches(TransactionFilters(type = TransactionType.EXPENSE)))
        assertFalse(lunch.matches(TransactionFilters(type = TransactionType.INCOME)))
    }

    @Test
    fun accountFilterMatchesEitherSideOfATransfer() {
        assertTrue(transfer.matches(TransactionFilters(accountId = 1)))
        assertTrue(transfer.matches(TransactionFilters(accountId = 2)))
        assertFalse(transfer.matches(TransactionFilters(accountId = 3)))
        assertTrue(lunch.matches(TransactionFilters(accountId = 1)))
        assertFalse(lunch.matches(TransactionFilters(accountId = 2)))
    }

    @Test
    fun categoryFilterExcludesTransfers() {
        assertTrue(lunch.matches(TransactionFilters(categoryId = 5)))
        assertFalse(lunch.matches(TransactionFilters(categoryId = 6)))
        assertFalse(transfer.matches(TransactionFilters(categoryId = 5)))
    }

    @Test
    fun filtersCombine() {
        assertTrue(
            lunch.matches(
                TransactionFilters(
                    TransactionType.EXPENSE,
                    accountId = 1,
                    categoryId = 5
                )
            )
        )
        assertFalse(
            lunch.matches(
                TransactionFilters(
                    TransactionType.EXPENSE,
                    accountId = 2,
                    categoryId = 5
                )
            )
        )
    }
}

class ShownTotalsTest {
    private fun row(type: TransactionType, amount: Long) = TransactionDetails(
        id = amount, type = type, amount = amount, title = "t", note = null, date = 0,
        categoryId = null, accountId = null, sourceAccountId = null, destinationAccountId = null,
        categoryName = null, categoryIconKey = null, accountName = null,
        sourceAccountName = null, destinationAccountName = null
    )

    @Test
    fun totalsSumIncomeAndExpenseAndCountEveryRow() {
        val totals = listOf(
            row(TransactionType.INCOME, 1000),
            row(TransactionType.EXPENSE, 300),
            row(TransactionType.EXPENSE, 200),
            row(TransactionType.TRANSFER, 999)
        ).totals()

        org.junit.Assert.assertEquals(1000L, totals.income)
        org.junit.Assert.assertEquals(500L, totals.expense)
        org.junit.Assert.assertEquals(4, totals.count)
    }
}
