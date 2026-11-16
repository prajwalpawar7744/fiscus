package com.prajwalpawar.fiscus.model

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** One day of transactions. [net] is income minus expense; null if none or mixed currencies. */
data class TransactionDaySection(
    val date: LocalDate,
    val items: List<TransactionListEntry>,
    val net: Money?,
)

/**
 * Groups transactions by local day in [timeZone] (ADR-022), resolving names. Transactions arrive
 * newest first, and `groupBy` keeps that order for days and items.
 */
fun List<Transaction>.groupByDay(
    timeZone: TimeZone,
    accounts: List<Account>,
    categories: List<Category>,
): List<TransactionDaySection> =
    groupBy { it.occurredAt.toLocalDateTime(timeZone).date }
        .map { (date, dayTransactions) ->
            TransactionDaySection(
                date = date,
                items = dayTransactions.toListEntries(accounts, categories),
                net = dayNet(dayTransactions),
            )
        }

/** Income minus expense for one day; null like [summarize]. */
fun dayNet(transactions: List<Transaction>): Money? = summarize(transactions)?.net
