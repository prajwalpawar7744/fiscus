package com.prajwalpawar.fiscus.ui.feature.accounts

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class AccountsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val cash = Account(1, "Cash", AccountType.CASH, "INR", Money(0, "INR"))
    private val oldCard =
        Account(2, "Old card", AccountType.CARD, "INR", Money(0, "INR"), archived = true)

    @Test
    fun showsActiveAndArchivedAndOpensAndAdds() {
        var opened: Long? = null
        var added = false
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                AccountsScreen(
                    uiState = AccountsUiState(
                        isLoading = false,
                        active = listOf(AccountWithBalance(cash, Money(458_000, "INR"), transactionCount = 3)),
                        archived = listOf(AccountWithBalance(oldCard, Money(0, "INR"))),
                    ),
                    onBack = {},
                    onOpenAccount = { opened = it },
                    onAddAccount = { added = true },
                )
            }
        }

        composeRule.onNodeWithText("Archived").assertIsDisplayed()
        composeRule.onNodeWithText("Old card", substring = true, useUnmergedTree = true)
            .assertIsDisplayed()
        // "Cash" is both the name and the type, so tap the row by its TalkBack label, which
        // includes the supporting line with the transaction count (ADR-041).
        composeRule.onNodeWithContentDescription("Cash, Cash · 3 transactions, Balance", substring = true)
            .performClick()
        assertEquals(1L, opened)
        composeRule.onNodeWithText("Add account", useUnmergedTree = true).performClick()
        assertTrue(added)
    }
}
