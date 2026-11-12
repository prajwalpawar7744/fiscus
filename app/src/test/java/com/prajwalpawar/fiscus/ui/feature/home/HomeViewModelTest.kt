package com.prajwalpawar.fiscus.ui.feature.home

import app.cash.turbine.test
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state shows the balance placeholder and empty message`() = runTest {
        val viewModel = HomeViewModel()
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(R.string.home_balance_placeholder, state.balance)
            assertEquals(R.string.home_empty, state.emptyMessage)
        }
    }
}
