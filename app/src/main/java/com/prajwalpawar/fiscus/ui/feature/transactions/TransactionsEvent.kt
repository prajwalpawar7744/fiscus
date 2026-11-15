package com.prajwalpawar.fiscus.ui.feature.transactions

import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.datetime.LocalDateRange

/** User actions on the Transactions screen, handled by [TransactionsViewModel.onEvent]. */
sealed interface TransactionsEvent {
    data class QueryChanged(val query: String) : TransactionsEvent
    data class TypeToggled(val type: TransactionType) : TransactionsEvent
    data class AccountToggled(val accountId: Long) : TransactionsEvent
    data class CategoryToggled(val categoryId: Long) : TransactionsEvent

    /** A picked range of local days (inclusive), or null to remove the date filter. */
    data class DateRangeChanged(val range: LocalDateRange?) : TransactionsEvent
    data class PickerOpened(val picker: FilterPicker) : TransactionsEvent
    data object PickerDismissed : TransactionsEvent

    /** Removes the filter that [picker] controls (its sheet's "Clear" button). */
    data class FilterCleared(val picker: FilterPicker) : TransactionsEvent

    /** Removes every chip filter (the search query is cleared by closing search). */
    data object FiltersCleared : TransactionsEvent
}
