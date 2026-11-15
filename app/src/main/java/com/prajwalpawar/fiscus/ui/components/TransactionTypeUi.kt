package com.prajwalpawar.fiscus.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
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
        TransactionType.EXPENSE -> R.drawable.ic_arrow_downward_rounded
        TransactionType.INCOME -> R.drawable.ic_arrow_upward_rounded
        TransactionType.TRANSFER -> R.drawable.ic_swap_horiz_rounded
    }

/** A container role with its matching on-role, for a [ShapeAccentIcon]. */
data class AccentRoles(val container: Color, val content: Color)

/**
 * The accent role pair for a [TransactionType], the same everywhere the type is shown (editor
 * hero, transaction list): expense → tertiary, income → secondary, transfer → primary.
 */
@Composable
@ReadOnlyComposable
fun TransactionType.accentRoles(): AccentRoles {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        TransactionType.EXPENSE -> AccentRoles(colors.tertiary, colors.onTertiary)
        TransactionType.INCOME -> AccentRoles(colors.secondary, colors.onSecondary)
        TransactionType.TRANSFER -> AccentRoles(colors.primary, colors.onPrimary)
    }
}

/**
 * The container role pair of a type's hero card (editor hero, detail hero; ADR-024):
 * expense → tertiaryContainer, income → secondaryContainer, transfer → surfaceContainerHighest.
 */
@Composable
@ReadOnlyComposable
fun TransactionType.heroRoles(): AccentRoles {
    val colors = MaterialTheme.colorScheme
    return when (this) {
        TransactionType.EXPENSE ->
            AccentRoles(colors.tertiaryContainer, colors.onTertiaryContainer)
        TransactionType.INCOME ->
            AccentRoles(colors.secondaryContainer, colors.onSecondaryContainer)
        TransactionType.TRANSFER -> AccentRoles(colors.surfaceContainerHighest, colors.onSurface)
    }
}
