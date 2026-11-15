package com.prajwalpawar.fiscus.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.TransactionType

/**
 * The order transaction types are shown in (FAB menu, type selector): Expense first, since it's
 * the default and by far the most common. Never rely on the enum's declaration order in the UI.
 */
val TransactionTypeDisplayOrder: List<TransactionType> =
    listOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.TRANSFER)

/** Label for a [TransactionType], shared by the FAB menu, editor and detail screens. */
@get:StringRes
val TransactionType.labelRes: Int
    get() = when (this) {
        TransactionType.EXPENSE -> R.string.transaction_type_expense
        TransactionType.INCOME -> R.string.transaction_type_income
        TransactionType.TRANSFER -> R.string.transaction_type_transfer
    }

/** Material Symbols Rounded icon for a [TransactionType] (ADR-010). */
@get:DrawableRes
val TransactionType.iconRes: Int
    get() = when (this) {
        TransactionType.EXPENSE -> R.drawable.ic_arrow_upward_rounded
        TransactionType.INCOME -> R.drawable.ic_arrow_downward_rounded
        TransactionType.TRANSFER -> R.drawable.ic_swap_horiz_rounded
    }
