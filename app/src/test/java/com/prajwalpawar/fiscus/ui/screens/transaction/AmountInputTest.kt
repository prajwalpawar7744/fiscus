package com.prajwalpawar.fiscus.ui.screens.transaction

import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmountInputTest {
    @Test
    fun acceptsPartialAndCompleteAmounts() {
        listOf("", "0", "12", "12.", "12.3", "12.34", ".5", "999999999999.99").forEach {
            assertTrue("expected '$it' to be accepted", isAmountInput(it))
        }
    }

    @Test
    fun rejectsExtraDecimalsNonDigitsAndOverlongAmounts() {
        listOf("12.345", "1.2.3", "12a", "-5", "1,5", " 1", "1234567890123").forEach {
            assertFalse("expected '$it' to be rejected", isAmountInput(it))
        }
    }

    @Test
    fun convertsToHundredths() {
        assertEquals(1250L, amountToMinorUnits("12.5"))
        assertEquals(1234L, amountToMinorUnits("12.34"))
        assertEquals(1200L, amountToMinorUnits("12."))
        assertEquals(50L, amountToMinorUnits(".5"))
        assertEquals(99999999999999L, amountToMinorUnits("999999999999.99"))
        assertNull(amountToMinorUnits(""))
        assertNull(amountToMinorUnits("."))
    }
}

class FindIssueTest {
    private val food = Category(id = 1, name = "Food", type = CategoryType.EXPENSE)

    private fun issue(
        form: TransactionForm,
        categories: List<Category> = listOf(food),
        accountCount: Int = 2,
        from: Long? = 10,
        to: Long? = null
    ) = findIssue(form, categories, accountCount, from, to)

    @Test
    fun expenseNeedsAmountThenCategory() {
        assertEquals(TransactionFormIssue.AMOUNT, issue(TransactionForm(amount = "0")))
        assertEquals(TransactionFormIssue.CATEGORY, issue(TransactionForm(amount = "5")))
        assertNull(issue(TransactionForm(amount = "5", categoryId = 1)))
    }

    @Test
    fun categoryFromAnotherTypeIsRejected() {
        // Category 99 isn't in the current (expense) list, e.g. left over from income.
        assertEquals(
            TransactionFormIssue.CATEGORY,
            issue(TransactionForm(amount = "5", categoryId = 99))
        )
    }

    @Test
    fun transferNeedsTwoAccountsAndADestination() {
        val transfer = TransactionForm(type = TransactionType.TRANSFER, amount = "5")

        assertEquals(TransactionFormIssue.NEEDS_TWO_ACCOUNTS, issue(transfer, accountCount = 1))
        assertEquals(TransactionFormIssue.TO_ACCOUNT, issue(transfer))
        assertNull(issue(transfer, to = 11))
    }
}
