package com.prajwalpawar.fiscus.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.CategoryTotal
import com.prajwalpawar.fiscus.data.model.MonthTotals
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.util.Period
import com.prajwalpawar.fiscus.util.monthSpanRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

// Months in the trend chart, ending at the period's last month.
internal const val TREND_MONTHS = 6

// How many of the month's largest expenses to list.
private const val LARGEST_EXPENSES = 5

data class AnalysisUiState(
    val isLoading: Boolean = true,
    // A month by default; can be a single day or any range.
    val period: Period = Period.currentMonth(),
    val income: Long = 0L,
    val spent: Long = 0L,
    // Against the same-length period just before; null when that had no spending.
    val changeVsPrevious: Int? = null,
    val dailyAverage: Long = 0L,
    // Null when there's no income this month.
    val savingsRate: Int? = null,
    // Which side the category breakdown shows.
    val categoryType: TransactionType = TransactionType.EXPENSE,
    // Largest first, with the tail folded into "Other".
    val categories: List<CategoryTotal> = emptyList(),
    // TREND_MONTHS entries, oldest first, ending at the period's last month.
    val trend: List<MonthTotals> = emptyList(),
    val largestExpenses: List<TransactionDetails> = emptyList()
) {
    val categoryTotal: Long get() = categories.sumOf { it.total }

    // Trend months the period touches; these are the emphasised columns.
    val highlightedMonths: Set<YearMonth>
        get() = generateSequence(YearMonth.from(period.start)) { it.plusMonths(1) }
            .takeWhile { !it.isAfter(YearMonth.from(period.end)) }
            .toSet()
}

class AnalysisViewModel(
    transactionRepository: TransactionRepository
) : ViewModel() {
    private val period = MutableStateFlow(Period.currentMonth())
    private val categoryType = MutableStateFlow(TransactionType.EXPENSE)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val trend = period.flatMapLatest { selected ->
        val last = YearMonth.from(selected.end)
        val first = last.minusMonths((TREND_MONTHS - 1).toLong())
        transactionRepository.observeMonthlyTotals(monthSpanRange(first, last))
    }

    // This period's totals and the same-length period's just before it.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val totals = period.flatMapLatest { selected ->
        combine(
            transactionRepository.observePeriodTotals(selected.toDateRange()),
            transactionRepository.observePeriodTotals(selected.previous().toDateRange()),
            ::Pair
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categories = combine(period, categoryType, ::Pair).flatMapLatest { (selected, type) ->
        transactionRepository.observeCategoryTotals(type, selected.toDateRange())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val largestExpenses = period.flatMapLatest { selected ->
        transactionRepository.observeLargestExpenses(selected.toDateRange(), LARGEST_EXPENSES)
    }

    val uiState: StateFlow<AnalysisUiState> =
        combine(
            combine(period, categoryType, ::Pair),
            totals,
            trend,
            categories,
            largestExpenses
        ) { (selected, type), (current, previous), trendRows, categoryRows, largest ->
            AnalysisUiState(
                isLoading = false,
                period = selected,
                income = current.income,
                spent = current.expense,
                changeVsPrevious = percentChange(previous.expense, current.expense),
                dailyAverage = dailyAverage(current.expense, selected),
                savingsRate = savingsRate(current.income, current.expense),
                categoryType = type,
                categories = foldCategories(categoryRows),
                trend = fillMonths(trendRows, last = YearMonth.from(selected.end), count = TREND_MONTHS),
                largestExpenses = largest
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalysisUiState()
        )

    // Ignores periods that start in the future.
    fun setPeriod(selected: Period) {
        if (!selected.start.isAfter(LocalDate.now())) period.value = selected
    }

    // Tapping a trend column jumps to that whole month.
    fun selectMonth(selected: YearMonth) {
        setPeriod(Period.month(selected))
    }

    fun setCategoryType(type: TransactionType) {
        categoryType.value = type
    }
}
