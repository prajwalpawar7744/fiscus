package com.prajwalpawar.fiscus.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.AccountBalance
import com.prajwalpawar.fiscus.data.model.PeriodTotals
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.util.monthRange
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.time.YearMonth

// How many transactions Home shows before "See all".
private const val RECENT_LIMIT = 10

data class HomeUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val accounts: List<AccountBalance> = emptyList(),
    val monthTotals: PeriodTotals = PeriodTotals(),
    val recent: List<TransactionDetails> = emptyList()
) {
    // Credit cards carry negative balances, so their debt lowers the total.
    val totalBalance: Long get() = accounts.sumOf { it.balance }

    // Share of this month's income already spent; null when there's no income to compare to.
    val spentShareOfIncome: Float?
        get() = if (monthTotals.income > 0L) {
            monthTotals.expense.toFloat() / monthTotals.income
        } else {
            null
        }
}

class HomeViewModel(
    accountRepository: AccountRepository,
    transactionRepository: TransactionRepository
) : ViewModel() {
    // shortcut: the month is fixed when Home opens; it won't roll over at midnight
    // on the 1st while the screen stays open. Re-read it on resume if that matters.
    private val month = YearMonth.now()

    val uiState: StateFlow<HomeUiState> =
        combine(
            accountRepository.accountBalances,
            transactionRepository.observePeriodTotals(monthRange(month)),
            transactionRepository.observeRecentTransactions(RECENT_LIMIT)
        ) { accounts, totals, recent ->
            HomeUiState(
                isLoading = false,
                month = month,
                accounts = accounts,
                monthTotals = totals,
                recent = recent
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState(month = month)
        )
}
