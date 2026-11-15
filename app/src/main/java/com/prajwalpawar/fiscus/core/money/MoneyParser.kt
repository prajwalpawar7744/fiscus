package com.prajwalpawar.fiscus.core.money

import com.prajwalpawar.fiscus.model.Money
import java.math.BigDecimal
import java.util.Currency

/** Text a user may type into an amount field: digits with at most one decimal separator. */
private val AmountInput = Regex("""\d*([.,]\d*)?""")

/** Longest amount text accepted while typing; keeps every valid input inside a [Long]. */
private const val MAX_AMOUNT_INPUT_LENGTH = 15

/** Number of minor-unit digits of [currencyCode] (2 for INR, 0 for JPY). */
fun currencyFractionDigits(currencyCode: String): Int =
    Currency.getInstance(currencyCode).defaultFractionDigits.coerceAtLeast(0)

/** True when [text] is acceptable while typing an amount (it may still be incomplete, e.g. "12."). */
fun isValidAmountInput(text: String): Boolean =
    text.length <= MAX_AMOUNT_INPUT_LENGTH && AmountInput.matches(text)

/**
 * Parses user input such as "19.99" (or "19,99") into [Money] of [currencyCode] without floating
 * point (ADR-003). Returns null for blank, malformed, zero, overflowing input, or input with more
 * decimals than the currency allows.
 */
fun parseMoneyInput(text: String, currencyCode: String): Money? {
    val normalized = text.trim().replace(',', '.')
    if (normalized.isEmpty() || normalized == "." || !AmountInput.matches(normalized)) return null
    val value = runCatching { BigDecimal(normalized) }.getOrNull() ?: return null
    val digits = currencyFractionDigits(currencyCode)
    if (value.stripTrailingZeros().scale() > digits) return null
    val minor = runCatching { value.movePointRight(digits).longValueExact() }.getOrNull()
    return minor?.takeIf { it > 0L }?.let { Money(it, currencyCode) }
}

/** Plain editable text for [money], e.g. 1999 INR → "19.99" (no symbol or grouping). */
fun formatMoneyInput(money: Money): String =
    BigDecimal.valueOf(money.amountMinor, currencyFractionDigits(money.currencyCode))
        .toPlainString()
