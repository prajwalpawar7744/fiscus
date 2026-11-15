package com.prajwalpawar.fiscus.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.TransactionType

/**
 * A transaction amount with its sign as text ("−₹450.00", "+₹50,000.00"; transfers unsigned).
 * The sign comes from `strings.xml` so it can be localized, and carries the meaning, never color
 * alone (DESIGN_SYSTEM.md "Accessibility"). On screen amounts show an arrow instead (ADR-032);
 * this text is what TalkBack reads, so the direction is still spoken.
 */
@Composable
fun signedAmountText(type: TransactionType, amount: Money): String {
    val formatted = formatMoney(amount)
    return when (type) {
        TransactionType.EXPENSE -> stringResource(R.string.amount_signed_expense, formatted)
        TransactionType.INCOME -> stringResource(R.string.amount_signed_income, formatted)
        TransactionType.TRANSFER -> formatted
    }
}

/** A net total (income minus expense) with its sign as text; zero is shown unsigned. */
@Composable
fun signedNetText(net: Money): String = when {
    net.isNegative -> signedAmountText(TransactionType.EXPENSE, -net)
    net.isZero -> formatMoney(net)
    else -> signedAmountText(TransactionType.INCOME, net)
}

/**
 * The direction arrow for a net total: down when money went out overall, up when it came in, none
 * at zero (the same icons as the types, `iconRes`; ADR-032).
 */
@DrawableRes
fun netIconRes(net: Money): Int? = when {
    net.isNegative -> TransactionType.EXPENSE.iconRes
    net.isZero -> null
    else -> TransactionType.INCOME.iconRes
}

/** A net total's size without its sign, shown after [netIconRes]'s arrow (ADR-032). */
fun netAmountText(net: Money): String = formatMoney(if (net.isNegative) -net else net)
