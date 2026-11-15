package com.prajwalpawar.fiscus.ui.feature.transactiondetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

/** Critical flow: the detail screen shows the transaction, and delete asks before deleting. */
class TransactionDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val now = Instant.fromEpochMilliseconds(1_763_200_000_000)
    private val loaded = TransactionDetailUiState(
        isLoading = false,
        transaction = Transaction(
            id = 1,
            type = TransactionType.EXPENSE,
            title = "Dinner out",
            amount = Money(125_000, "INR"),
            accountId = 1,
            categoryId = 1,
            note = "Dinner",
            occurredAt = now,
            createdAt = now,
            updatedAt = now,
        ),
        occurredAt = LocalDateTime(2026, 11, 15, 19, 30),
        account = Account(1, "Cash", AccountType.CASH, "INR", Money(0, "INR")),
        category = Category(1, "Food", CategoryKind.EXPENSE, "restaurant"),
    )

    @Test
    fun showsDetailsAndConfirmsBeforeDeleting() {
        var state by mutableStateOf(loaded)
        val events = mutableListOf<TransactionDetailEvent>()
        var edited = false
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                TransactionDetailScreen(
                    uiState = state,
                    onEvent = { event ->
                        events += event
                        when (event) {
                            TransactionDetailEvent.DeleteRequested -> state = state.copy(showDeleteConfirmation = true)
                            TransactionDetailEvent.DeleteDismissed -> state = state.copy(showDeleteConfirmation = false)
                            TransactionDetailEvent.DeleteConfirmed -> Unit
                        }
                    },
                    onBack = {},
                    onEdit = { edited = true },
                )
            }
        }

        composeRule.onNodeWithText("Dinner out").assertIsDisplayed()
        composeRule.onNodeWithText("Cash").assertIsDisplayed()
        // The hero reads as one node: "Expense · Food, ₹1,250.00".
        composeRule.onNode(hasContentDescription("Expense · Food", substring = true))
            .assertIsDisplayed()
        composeRule.onNodeWithText("Dinner").assertIsDisplayed()

        composeRule.onNode(hasText("Edit") and hasClickAction()).performClick()
        assertTrue(edited)

        // Delete is an icon button in the floating toolbar, found by its content description.
        composeRule.onNodeWithContentDescription("Delete").performClick()
        composeRule.onNodeWithText("Delete transaction?").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("Delete transaction?").assertDoesNotExist()

        composeRule.onNodeWithContentDescription("Delete").performClick()
        // The dialog's confirm button is the only "Delete" text button.
        composeRule.onNode(hasText("Delete") and hasClickAction()).performClick()

        assertEquals(
            listOf(
                TransactionDetailEvent.DeleteRequested,
                TransactionDetailEvent.DeleteDismissed,
                TransactionDetailEvent.DeleteRequested,
                TransactionDetailEvent.DeleteConfirmed,
            ),
            events,
        )
    }
}
