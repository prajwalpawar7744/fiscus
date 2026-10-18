package com.prajwalpawar.fiscus.ui.screens.transaction

import com.prajwalpawar.fiscus.data.model.Category
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.data.model.TransactionItem
import com.prajwalpawar.fiscus.data.model.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ItemBreakdownTest {
    private val food = Category(id = 1L, name = "Food", type = CategoryType.EXPENSE, iconKey = "food")

    @Test
    fun itemTotalIsQuantityTimesPriceRoundedHalfUp() {
        assertEquals(12000L, TransactionItem("Milk", quantity = 200L, unitPrice = 6000L).total) // 2 × 60
        assertEquals(4500L, TransactionItem("Rice", quantity = 150L, unitPrice = 3000L).total)  // 1.5 × 30
        assertEquals(413L, TransactionItem("Bolt", quantity = 33L, unitPrice = 1250L).total)   // 0.33 × 12.50 = 4.125 → 4.13
    }

    @Test
    fun amountIsSumOfItemsAndIgnoresTypedAmount() {
        val form = TransactionForm(
            amount = "999",
            items = listOf(
                ItemDraft(key = 0, name = "Milk", quantity = "2", price = "60"),
                ItemDraft(key = 1, name = "Bread", quantity = "1", price = "40.5")
            )
        )
        assertEquals("160.5", form.effectiveAmount)
    }

    @Test
    fun transferIgnoresItems() {
        val form = TransactionForm(
            type = TransactionType.TRANSFER,
            amount = "50",
            items = listOf(ItemDraft(key = 0, name = "Milk", quantity = "1", price = "10"))
        )
        assertEquals("50", form.effectiveAmount)
    }

    @Test
    fun incompleteItemBlocksSubmitFirst() {
        val form = TransactionForm(
            title = "Groceries",
            categoryId = food.id,
            items = listOf(
                ItemDraft(key = 0, name = "Milk", quantity = "2", price = "60"),
                ItemDraft(key = 1, name = "", quantity = "1", price = "40")
            )
        )
        assertEquals(TransactionFormIssue.ITEMS, findIssue(form, listOf(food), 1, 10L, null))
    }

    @Test
    fun completeItemsAreSubmittable() {
        val form = TransactionForm(
            title = "Groceries",
            categoryId = food.id,
            items = listOf(ItemDraft(key = 0, name = "Milk", quantity = "2", price = "60"))
        )
        assertNull(findIssue(form, listOf(food), 1, 10L, null))
    }
}
