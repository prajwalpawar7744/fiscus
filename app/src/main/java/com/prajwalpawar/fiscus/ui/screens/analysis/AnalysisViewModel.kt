package com.prajwalpawar.fiscus.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.CategoryTotal
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

// Months of context around a whole month in the trend chart.
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
    // Every transaction in the period, transfers included.
    val transactionCount: Int = 0,
    // Null when there's no income in the period.
    val savingsRate: Int? = null,
    // Which side the category breakdown shows.
    val categoryType: TransactionType = TransactionType.EXPENSE,
    // Largest first, with the tail folded into "Other".
    val categories: List<CategoryTotal> = emptyList(),
    // How the trend buckets the period; null hides the trend (a single day).
    val trendScale: TrendScale? = TrendScale.MONTH_CONTEXT,
    // Oldest first.
    val trend: List<TrendBucket> = emptyList(),
    val largestExpenses: List<TransactionDetails> = emptyList()
) {
    val categoryTotal: Long get() = categories.sumOf { it.total }
}

class AnalysisViewModel(
    transactionRepository: TransactionRepository
) : ViewModel() {
    private val period = MutableStateFlow(Period.currentMonth())
    private val categoryType = MutableStateFlow(TransactionType.EXPENSE)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val trend = period.flatMapLatest { selected ->
        val firstMonth = YearMonth.from(selected.start)
        val lastMonth = YearMonth.from(selected.end)

        when (trendScale(selected)) {
            null -> flowOf(emptyList())

            // The month among the TREND_MONTHS before it, emphasised.
            TrendScale.MONTH_CONTEXT -> {
                val first = lastMonth.minusMonths((TREND_MONTHS - 1).toLong())
                transactionRepository.observeMonthlyTotals(monthSpanRange(first, lastMonth))
                    .map { rows -> monthlyBuckets(rows, first, lastMonth) { it == lastMonth } }
            }

            TrendScale.DAILY ->
                transactionRepository.observeDailyTotals(selected.toDateRange())
                    .map { rows -> dailyBuckets(rows, selected) }

            // Queried over the period itself, so edge months only count days inside it.
            TrendScale.MONTHLY ->
                transactionRepository.observeMonthlyTotals(selected.toDateRange())
                    .map { rows -> monthlyBuckets(rows, firstMonth, lastMonth) { true } }
        }
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
                transactionCount = current.count,
                savingsRate = savingsRate(current.income, current.expense),
                categoryType = type,
                categories = foldCategories(categoryRows),
                trendScale = trendScale(selected),
                trend = trendRows,
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

    fun setCategoryType(type: TransactionType) {
        categoryType.value = type
    }
}
