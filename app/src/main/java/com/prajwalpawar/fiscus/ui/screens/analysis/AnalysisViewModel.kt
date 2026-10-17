package com.prajwalpawar.fiscus.ui.screens.analysis

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prajwalpawar.fiscus.data.model.CategoryTotal
import com.prajwalpawar.fiscus.data.model.MonthTotals
import com.prajwalpawar.fiscus.data.model.TransactionDetails
import com.prajwalpawar.fiscus.data.model.TransactionType
import com.prajwalpawar.fiscus.data.repository.TransactionRepository
import com.prajwalpawar.fiscus.util.monthRange
import com.prajwalpawar.fiscus.util.monthSpanRange
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.YearMonth

// Months in the trend chart, ending at the selected month.
internal const val TREND_MONTHS = 6

// How many of the month's largest expenses to list.
private const val LARGEST_EXPENSES = 5

data class AnalysisUiState(
    val isLoading: Boolean = true,
    val month: YearMonth = YearMonth.now(),
    val income: Long = 0L,
    val spent: Long = 0L,
    // Null when last month had no spending to compare with.
    val changeVsLastMonth: Int? = null,
    val dailyAverage: Long = 0L,
    // Null when there's no income this month.
    val savingsRate: Int? = null,
    // Which side the category breakdown shows.
    val categoryType: TransactionType = TransactionType.EXPENSE,
    // Largest first, with the tail folded into "Other".
    val categories: List<CategoryTotal> = emptyList(),
    // TREND_MONTHS entries, oldest first, ending at [month].
    val trend: List<MonthTotals> = emptyList(),
    val largestExpenses: List<TransactionDetails> = emptyList()
) {
    val canGoForward: Boolean get() = month < YearMonth.now()
    val categoryTotal: Long get() = categories.sumOf { it.total }
}

class AnalysisViewModel(
    transactionRepository: TransactionRepository
) : ViewModel() {
    private val month = MutableStateFlow(YearMonth.now())
    private val categoryType = MutableStateFlow(TransactionType.EXPENSE)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val trend = month.flatMapLatest { selected ->
        val first = selected.minusMonths((TREND_MONTHS - 1).toLong())
        transactionRepository.observeMonthlyTotals(monthSpanRange(first, selected))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val categories = combine(month, categoryType, ::Pair).flatMapLatest { (selected, type) ->
        transactionRepository.observeCategoryTotals(type, monthRange(selected))
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val largestExpenses = month.flatMapLatest { selected ->
        transactionRepository.observeLargestExpenses(monthRange(selected), LARGEST_EXPENSES)
    }

    val uiState: StateFlow<AnalysisUiState> =
        combine(month, categoryType, trend, categories, largestExpenses) { selected, type, trendRows, categoryRows, largest ->
            val months = fillMonths(trendRows, last = selected, count = TREND_MONTHS)
            val current = months.last()
            val previous = months[months.size - 2]

            AnalysisUiState(
                isLoading = false,
                month = selected,
                income = current.income,
                spent = current.expense,
                changeVsLastMonth = percentChange(previous.expense, current.expense),
                dailyAverage = dailyAverage(current.expense, selected),
                savingsRate = savingsRate(current.income, current.expense),
                categoryType = type,
                categories = foldCategories(categoryRows),
                trend = months,
                largestExpenses = largest
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AnalysisUiState()
        )

    fun previousMonth() {
        month.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        month.update { if (it < YearMonth.now()) it.plusMonths(1) else it }
    }

    // Tapping a trend column jumps to that month.
    fun selectMonth(selected: YearMonth) {
        if (selected <= YearMonth.now()) month.value = selected
    }

    fun setCategoryType(type: TransactionType) {
        categoryType.value = type
    }
}
