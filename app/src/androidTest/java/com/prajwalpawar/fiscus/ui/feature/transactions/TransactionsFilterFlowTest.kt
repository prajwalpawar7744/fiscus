package com.prajwalpawar.fiscus.ui.feature.transactions

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeUp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.test.platform.app.InstrumentationRegistry
import com.prajwalpawar.fiscus.data.db.FiscusDatabase
import com.prajwalpawar.fiscus.data.db.SeedCallback
import com.prajwalpawar.fiscus.data.repository.OfflineFirstAccountRepository
import com.prajwalpawar.fiscus.data.repository.OfflineFirstCategoryRepository
import com.prajwalpawar.fiscus.data.repository.OfflineFirstTransactionRepository
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.time.Clock

/**
 * The filter sheets with the real ViewModel, repositories and an in-memory seeded database, so
 * every toggle re-queries and re-filters the list under the sheet, as in the app.
 */
class TransactionsFilterFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var database: FiscusDatabase
    private lateinit var viewModel: TransactionsViewModel

    @Before
    fun setUp() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, FiscusDatabase::class.java)
            .setDriver(BundledSQLiteDriver())
            .addCallback(SeedCallback(context, currencyCode = "INR"))
            .build()
        val io = Dispatchers.IO
        val transactions = OfflineFirstTransactionRepository(database.transactionDao(), io)
        val accounts = OfflineFirstAccountRepository(database.accountDao(), io)
        val categories = OfflineFirstCategoryRepository(database.categoryDao(), io)
        val cash = accounts.observeAccounts().first().single()
        val all = categories.observeCategories().first()
        val now = Clock.System.now()
        all.filter { it.kind == CategoryKind.EXPENSE }.take(4).forEach { category ->
            transactions.save(
                Transaction(
                    type = TransactionType.EXPENSE,
                    title = category.name,
                    amount = Money(10_000, "INR"),
                    accountId = cash.id,
                    categoryId = category.id,
                    occurredAt = now,
                    createdAt = now,
                    updatedAt = now,
                ),
            )
        }
        viewModel = TransactionsViewModel(
            transactionRepository = transactions,
            accountRepository = accounts,
            categoryRepository = categories,
            clock = Clock.System,
            timeZone = TimeZone.currentSystemDefault(),
        )
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun categorySheetStaysResponsiveWhileTheListFilters() {
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                TransactionsScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onOpenSettings = {},
                    onAddTransaction = {},
                    onOpenTransaction = {},
                )
            }
        }
        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
        viewModel.onEvent(TransactionsEvent.PickerOpened(FilterPicker.CATEGORY))
        composeRule.waitForIdle()

        sheetRow("Groceries").performClick()
        composeRule.waitForIdle()
        sheetRow("Transport").performClick()
        composeRule.waitForIdle()

        assertEquals(2, viewModel.uiState.value.selectedCategoryIds.size)
    }

    @Test
    fun choosingACategoryWithNoTransactionsKeepsTheSheetResponsive() {
        composeRule.setContent {
            FiscusTheme(dynamicColor = false) {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                TransactionsScreen(
                    uiState = uiState,
                    onEvent = viewModel::onEvent,
                    onOpenSettings = {},
                    onAddTransaction = {},
                    onOpenTransaction = {},
                )
            }
        }
        composeRule.waitUntil(5_000) { !viewModel.uiState.value.isLoading }
        viewModel.onEvent(TransactionsEvent.PickerOpened(FilterPicker.CATEGORY))
        composeRule.waitForIdle()

        // Reach the income categories the way a finger does: swipe the sheet up (this first
        // expands the half-height sheet, then scrolls the list), twice.
        repeat(2) {
            sheetRow("Groceries").performTouchInput { swipeUp() }
            composeRule.waitForIdle()
        }
        // "Salary" has no transactions, so the list under the sheet becomes empty.
        composeRule.onNode(hasText("Salary") and hasClickAction()).performClick()
        composeRule.waitForIdle()
        composeRule.onNode(hasText("Gifts") and hasClickAction()).performClick()
        composeRule.waitForIdle()

        assertEquals(2, viewModel.uiState.value.selectedCategoryIds.size)
    }

    /** [name] also titles a list row; the sheet is composed last, so its row is the last match. */
    private fun sheetRow(name: String) =
        composeRule.onAllNodes(hasText(name) and hasClickAction()).let { nodes ->
            nodes[nodes.fetchSemanticsNodes().lastIndex]
        }
}
