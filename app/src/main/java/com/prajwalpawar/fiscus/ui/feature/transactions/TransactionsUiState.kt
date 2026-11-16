package com.prajwalpawar.fiscus.ui.feature.transactions

import com.prajwalpawar.fiscus.model.Account
import com.prajwalpawar.fiscus.model.Category
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.PeriodSummary
import com.prajwalpawar.fiscus.model.TransactionDaySection
import com.prajwalpawar.fiscus.model.TransactionListEntry
import com.prajwalpawar.fiscus.model.TransactionType
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateRange

/** Immutable UI state for the Transactions screen (ADR-022). */
data class TransactionsUiState(
    val isLoading: Boolean = true,
    /** The user's current day (injected clock and time zone), for "Today" / "Yesterday". */
    val today: LocalDate? = null,
    /** Matching transactions grouped by local day, newest day first. */
    val sections: List<TransactionDaySection> = emptyList(),
    val selectedTypes: Set<TransactionType> = emptySet(),
    val selectedAccountIds: Set<Long> = emptySet(),
    val selectedCategoryIds: Set<Long> = emptySet(),
    /** A picked range; null means the default, **this month** (ADR-029). */
    val dateRange: LocalDateRange? = null,
    /** The search query as typed; results show in the expanded search bar and highlight it. */
    val query: String = "",
    /** All accounts and categories for the pickers, including archived ones old rows refer to. */
    val accounts: List<Account> = emptyList(),
    val categories: List<Category> = emptyList(),
    val openPicker: FilterPicker? = null,
    /** Totals of what the list shows; null when it's empty or mixes currencies (ADR-023). */
    val summary: PeriodSummary? = null,
) {
    val isSearching: Boolean get() = query.isNotBlank()

    /** How many transactions the list (or search) shows. */
    val resultCount: Int get() = sections.sumOf { it.items.size }

    val hasChipFilters: Boolean
        get() = selectedTypes.isNotEmpty() || selectedAccountIds.isNotEmpty() ||
            selectedCategoryIds.isNotEmpty() || dateRange != null

    /** Nothing to show although filters are set: offer to clear them, not "add a transaction". */
    val isFilteredEmpty: Boolean get() = !isLoading && sections.isEmpty() && hasChipFilters
}

/** The filter picker that is open, if any. */
enum class FilterPicker { TYPE, ACCOUNT, CATEGORY, DATE }
