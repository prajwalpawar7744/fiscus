package com.prajwalpawar.fiscus.ui.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.core.money.defaultCurrencyCode
import com.prajwalpawar.fiscus.core.time.currentMonth
import com.prajwalpawar.fiscus.data.repository.AccountRepository
import com.prajwalpawar.fiscus.data.repository.CategoryRepository
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.model.AccountWithBalance
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.PeriodSummary
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
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
            monthSummary = monthSummary(monthTransactions, total(active)),
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

/** Sum of the balances; zero with no accounts, null with mixed currencies (ADR-016). */
internal fun total(accounts: List<AccountWithBalance>): Money? {
    // No accounts is a total of zero, not "several currencies".
    if (accounts.isEmpty()) return Money.zero(defaultCurrencyCode())
    val currency = accounts.map { it.balance.currencyCode }.distinct().singleOrNull() ?: return null
    return accounts.fold(Money.zero(currency)) { sum, account -> sum + account.balance }
}

/**
 * This month's totals for the hero tiles: zeros when nothing happened yet (the tiles always show;
 * only the spent ring hides, ADR-037). Null only when currencies are mixed.
 */
internal fun monthSummary(transactions: List<Transaction>, total: Money?): PeriodSummary? =
    summarize(transactions) ?: total
        ?.takeIf { transactions.none { it.type != TransactionType.TRANSFER } }
        ?.let { PeriodSummary(Money.zero(it.currencyCode), Money.zero(it.currencyCode)) }
