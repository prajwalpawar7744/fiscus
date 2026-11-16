@file:OptIn(ExperimentalCoroutinesApi::class) // advanceUntilIdle

package com.prajwalpawar.fiscus.ui.feature.accounts

import com.prajwalpawar.fiscus.testing.BankAccount
import com.prajwalpawar.fiscus.testing.CashAccount
import com.prajwalpawar.fiscus.testing.FakeAccountRepository
import com.prajwalpawar.fiscus.testing.FakeCategoryRepository
import com.prajwalpawar.fiscus.testing.FakeTransactionRepository
import com.prajwalpawar.fiscus.testing.FixedClock
import com.prajwalpawar.fiscus.testing.FoodCategory
import com.prajwalpawar.fiscus.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse

class AccountsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val accounts = FakeAccountRepository()

    private fun TestScope.viewModel() = AccountsViewModel(accounts).also { viewModel ->
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect()
        }
        advanceUntilIdle()
    }

    @Test
    fun `splits active and archived accounts`() = runTest(mainDispatcherRule.testDispatcher) {
        accounts.accounts.value = listOf(CashAccount, BankAccount.copy(archived = true))

        val state = viewModel().uiState.value

        assertFalse(state.isLoading)
        assertEquals(listOf("Cash"), state.active.map { it.account.name })
        assertEquals(listOf("Bank"), state.archived.map { it.account.name })
    }
}
