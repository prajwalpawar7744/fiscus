package com.prajwalpawar.fiscus.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.core.time.currentMonth
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.TransactionFilter
import com.prajwalpawar.fiscus.model.summarize
import com.prajwalpawar.fiscus.model.toListEntries
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.time.Clock
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

/**
 * Screen-level state holder for Home (ADR-033): the total balance hero, this month's income and
 * expense, the accounts and the newest transactions, all from repository streams via `stateIn`.
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    transactionRepository: TransactionRepository,
    accountRepository: AccountRepository,
    categoryRepository: CategoryRepository,
    clock: Clock,
    timeZone: TimeZone,
) : ViewModel() {

    private val month = currentMonth(clock.now().toLocalDateTime(timeZone).date)

    val uiState: StateFlow<HomeUiState> = combine(
        accountRepository.observeAccountsWithBalance(),
        categoryRepository.observeCategories(),
        transactionRepository.observeTransactions(),
        transactionRepository.observeTransactions(
            TransactionFilter(
                from = month.start.atStartOfDayIn(timeZone),
                toExclusive = month.endInclusive.plus(DatePeriod(days = 1))
                    .atStartOfDayIn(timeZone),
            ),
        ),
    ) { accountsWithBalance, categories, transactions, monthTransactions ->
        val active = accountsWithBalance.filterNot { it.account.archived }
        HomeUiState(
            isLoading = false,
            totalBalance = total(active),
            accounts = active,
            monthSummary = summarize(monthTransactions),
            recent = transactions
                .take(RecentTransactionCount)
                .toListEntries(accountsWithBalance.map { it.account }, categories),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeUiState(),
    )
}

/** Sum of the balances; null with no accounts or mixed currencies (no conversion, ADR-016). */
internal fun total(accounts: List<AccountWithBalance>): Money? {
    val currency = accounts.map { it.balance.currencyCode }.distinct().singleOrNull() ?: return null
    return accounts.fold(Money.zero(currency)) { sum, account -> sum + account.balance }
}
