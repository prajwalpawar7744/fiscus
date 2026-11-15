package com.prajwalpawar.fiscus.model

/** Income and expense totals (transfers excluded, DOMAIN.md), in one currency. */
data class PeriodSummary(val income: Money, val expense: Money) {
    val net: Money get() = income - expense
}

/**
 * Income and expense totals; transfers don't count (DOMAIN.md). Null when there's no income or
 * expense, or the currencies are mixed (no conversion yet, ADR-016).
 */
fun summarize(transactions: List<Transaction>): PeriodSummary? {
    val counted = transactions.filter { it.type != TransactionType.TRANSFER }
    val currency = counted.map { it.amount.currencyCode }.distinct().singleOrNull() ?: return null
    val zero = Money.zero(currency)
    return counted.fold(PeriodSummary(zero, zero)) { summary, transaction ->
        when (transaction.type) {
            TransactionType.INCOME -> summary.copy(income = summary.income + transaction.amount)
            else -> summary.copy(expense = summary.expense + transaction.amount)
        }
    }
}
