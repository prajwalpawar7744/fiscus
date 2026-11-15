package com.prajwalpawar.fiscus.ui.feature.transactioneditor

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import kotlinx.datetime.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Critical flow (CONVENTIONS.md): entering an expense enables Save and sends the Save event. */
class TransactionEditorScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val cash = Account(1, "Cash", AccountType.CASH, "INR", Money(0, "INR"))
    private val food = Category(1, "Food", CategoryKind.EXPENSE, "restaurant")

    @Test
    fun enteringAmountAndCategoryEnablesSave() {
        var state by mutableStateOf(
            TransactionEditorUiState(
                isLoading = false,
                // Edit mode: no autofocus, so the soft keyboard doesn't cover the options in the
                // plain test host (LESSONS.md #8). Autofocus isn't what this test checks.
                isNew = false,
                accounts = listOf(cash),
                accountId = cash.id,
                categories = listOf(food),
                date = LocalDate(2026, 11, 15),
            ),
        )
        var saved = false
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                TransactionEditorScreen(
                    uiState = state,
                    onEvent = { event ->
                        when (event) {
                            is TransactionEditorEvent.AmountChanged -> state = state.copy(amountText = event.text)
                            is TransactionEditorEvent.TitleChanged -> state = state.copy(title = event.title)
                            is TransactionEditorEvent.CategorySelected -> state = state.copy(categoryId = event.categoryId)
                            TransactionEditorEvent.Save -> saved = true
                            else -> Unit
                        }
                    },
                    onBack = {},
                )
            }
        }

        composeRule.onNodeWithText("Save").assertIsNotEnabled()
        composeRule.onNodeWithText("Food").performClick()
        composeRule.onNodeWithContentDescription("Amount").performTextInput("250")
        composeRule.onNodeWithText("Title").performTextInput("Team lunch")
        composeRule.onNodeWithText("Save").assertIsEnabled().performClick()

        assertTrue(saved)
    }
}
