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
        assertTrue(lunch.matches(TransactionFilters(TransactionType.EXPENSE, accountId = 1, categoryId = 5)))
        assertFalse(lunch.matches(TransactionFilters(TransactionType.EXPENSE, accountId = 2, categoryId = 5)))
    }
}
