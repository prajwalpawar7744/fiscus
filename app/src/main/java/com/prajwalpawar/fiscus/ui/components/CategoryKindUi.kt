package com.prajwalpawar.fiscus.ui.components

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import com.prajwalpawar.fiscus.model.CategoryKind
import com.prajwalpawar.fiscus.model.TransactionType

/** Order of the kinds in choices and tabs: expense first, like [TransactionTypeDisplayOrder]. */
val CategoryKindDisplayOrder: List<CategoryKind> = listOf(CategoryKind.EXPENSE, CategoryKind.INCOME)

/** The transaction type a [CategoryKind] classifies; its label, icon and accents come from it. */
val CategoryKind.transactionType: TransactionType
    get() = when (this) {
        CategoryKind.EXPENSE -> TransactionType.EXPENSE
        CategoryKind.INCOME -> TransactionType.INCOME
    }

@get:StringRes
val CategoryKind.labelRes: Int get() = transactionType.labelRes

@get:DrawableRes
val CategoryKind.iconRes: Int get() = transactionType.iconRes

/** The accent pair of the kind's transactions (ADR-023): expense tertiary, income secondary. */
@Composable
fun CategoryKind.accentRoles(): AccentRoles = transactionType.accentRoles()

/** The kind's hero card pair, as on the transaction page (ADR-025). */
@Composable
fun CategoryKind.heroRoles(): AccentRoles = transactionType.heroRoles()
