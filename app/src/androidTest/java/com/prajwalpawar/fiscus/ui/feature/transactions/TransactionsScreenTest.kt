package com.prajwalpawar.fiscus.ui.feature.transactions

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.AccountType
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import kotlin.time.Instant

/** Critical flow: the list shows days and items, filters send events, and a tap opens detail. */
class TransactionsScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val now = Instant.fromEpochMilliseconds(1_763_200_000_000)
    private val cash = Account(1, "Cash", AccountType.CASH, "INR", Money(0, "INR"))
    private val food = Category(1, "Food", CategoryKind.EXPENSE, "restaurant")
    private val lunch = Transaction(
        id = 7,
        type = TransactionType.EXPENSE,
        title = "Lunch with team",
        amount = Money(45_000, "INR"),
        accountId = cash.id,
        categoryId = food.id,
        occurredAt = now,
        createdAt = now,
        updatedAt = now,
    )
    private val today = LocalDate(2026, 11, 15)
    private val loaded = TransactionsUiState(
        isLoading = false,
        today = today,
        sections = listOf(
            TransactionDaySection(
                date = today,
                items = listOf(
                    TransactionListEntry(
                        transaction = lunch,
                        occurredAt = LocalDateTime(2026, 11, 15, 13, 0),
                        account = cash,
                        toAccount = null,
                        category = food,
                    ),
                ),
                net = Money(-45_000, "INR"),
            ),
        ),
        accounts = listOf(cash),
        categories = listOf(food),
    )

    @Test
    fun showsGroupedItemsAndOpensDetailOnTap() {
        val events = mutableListOf<TransactionsEvent>()
        var opened: Long? = null
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                TransactionsScreen(
                    uiState = loaded,
                    onEvent = { events += it },
                    onOpenSettings = {},
                    onAddTransaction = {},
                    onOpenTransaction = { opened = it },
                )
            }
        }

        composeRule.onNodeWithText("Today").assertIsDisplayed()
        composeRule.onNodeWithText("Food · Cash").assertIsDisplayed()

        composeRule.onNode(hasText("Type") and hasClickAction()).performClick()
        assertEquals(TransactionsEvent.PickerOpened(FilterPicker.TYPE), events.last())

        composeRule.onNodeWithText("Lunch with team").performClick()
        assertEquals(7L, opened)
    }

    @Test
    fun typeSheetTogglesAndClears() {
        val events = mutableListOf<TransactionsEvent>()
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                TransactionsScreen(
                    uiState = loaded.copy(
                        openPicker = FilterPicker.TYPE,
                        selectedTypes = setOf(TransactionType.EXPENSE),
                    ),
                    onEvent = { events += it },
                    onOpenSettings = {},
                    onAddTransaction = {},
                    onOpenTransaction = {},
                )
            }
        }

        composeRule.onNode(hasText("Income") and hasClickAction()).performClick()
        assertEquals(TransactionsEvent.TypeToggled(TransactionType.INCOME), events.last())
        composeRule.onNode(hasText("Clear") and hasClickAction()).performClick()
        assertEquals(TransactionsEvent.FilterCleared(FilterPicker.TYPE), events.last())
        composeRule.onNode(hasText("Done") and hasClickAction()).performClick()
        assertEquals(TransactionsEvent.PickerDismissed, events.last())
    }

    @Test
    fun categorySheetTogglesCategories() {
        val events = mutableListOf<TransactionsEvent>()
        val names = listOf(
            "Food & dining", "Groceries", "Transport", "Bills & utilities", "Shopping", "Health",
            "Entertainment", "Education", "Other expense",
        )
        val expense = names.mapIndexed { i, name ->
            Category(i + 1L, name, CategoryKind.EXPENSE, "restaurant")
        }
        val income = listOf("Salary", "Business", "Gifts", "Other income").mapIndexed { i, name ->
            Category(100L + i, name, CategoryKind.INCOME, "work")
        }
        // Stateful like the real ViewModel: each toggle changes the selection and redraws the sheet.
        var state by mutableStateOf(
            loaded.copy(categories = expense + income, openPicker = FilterPicker.CATEGORY),
        )
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                TransactionsScreen(
                    uiState = state,
                    onEvent = { event ->
                        events += event
                        if (event is TransactionsEvent.CategoryToggled) {
                            val ids = state.selectedCategoryIds
                            state = state.copy(
                                selectedCategoryIds = if (event.categoryId in ids) {
                                    ids - event.categoryId
                                } else {
                                    ids + event.categoryId
                                },
                            )
                        }
                    },
                    onOpenSettings = {},
                    onAddTransaction = {},
                    onOpenTransaction = {},
                )
            }
        }
        composeRule.waitForIdle()

        composeRule.onNode(hasText("Groceries") and hasClickAction()).performClick()
        assertEquals(TransactionsEvent.CategoryToggled(2L), events.last())
        composeRule.onNode(hasText("Transport") and hasClickAction()).performClick()
        assertEquals(TransactionsEvent.CategoryToggled(3L), events.last())
        composeRule.onNode(hasText("Salary") and hasClickAction()).performScrollTo().performClick()
        assertEquals(TransactionsEvent.CategoryToggled(100L), events.last())
    }

    @Test
    fun filteredEmptyOffersToClearFilters() {
        val events = mutableListOf<TransactionsEvent>()
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                TransactionsScreen(
                    uiState = loaded.copy(
                        sections = emptyList(),
                        selectedTypes = setOf(TransactionType.TRANSFER),
                    ),
                    onEvent = { events += it },
                    onOpenSettings = {},
                    onAddTransaction = {},
                    onOpenTransaction = {},
                )
            }
        }

        composeRule.onNodeWithText("No transactions match these filters.").assertIsDisplayed()
        // Both the chip row and the empty state offer "Clear filters"; the empty state's is last.
        composeRule.onAllNodes(hasText("Clear filters") and hasClickAction()).let { nodes ->
            nodes[nodes.fetchSemanticsNodes().lastIndex].performClick()
        }
        assertEquals(TransactionsEvent.FiltersCleared, events.last())
    }
}
