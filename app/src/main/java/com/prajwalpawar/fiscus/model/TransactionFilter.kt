package com.prajwalpawar.fiscus.model

import kotlin.time.Instant

/**
 * What the transaction list shows (ADR-022). An empty set or a null bound means "no restriction".
 * [query] matches the title or note, ignoring case. [accountIds] matches either side of a transfer.
 * The date range is `[from, toExclusive)`.
 */
data class TransactionFilter(
    val query: String = "",
    val types: Set<TransactionType> = emptySet(),
    val accountIds: Set<Long> = emptySet(),
    val categoryIds: Set<Long> = emptySet(),
    val from: Instant? = null,
    val toExclusive: Instant? = null,
) {
    /** True when any chip filter is set (the search [query] is shown separately). */
    val hasChipFilters: Boolean
        get() = types.isNotEmpty() || accountIds.isNotEmpty() || categoryIds.isNotEmpty() ||
            from != null || toExclusive != null

    val isActive: Boolean get() = query.isNotBlank() || hasChipFilters
}
