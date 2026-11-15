package com.prajwalpawar.fiscus.core.money

import android.icu.text.NumberFormat
import com.prajwalpawar.fiscus.model.Money
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

/** Currency used when the device locale has no country (e.g. plain "en"). */
const val FALLBACK_CURRENCY_CODE = "INR"

/**
 * Formats [money] for display, e.g. 1999 INR → "₹19.99", 20000 INR → "₹200" (ADR-029: decimals
 * only when there are any). Exact: no floating point involved.
 * Uses Android's ICU formatter, which applies locale grouping such as Indian lakh/crore
 * ("₹1,24,580.00"); `java.text` on the JVM does not.
 */
fun formatMoney(money: Money, locale: Locale = Locale.getDefault()): String {
    val currency = android.icu.util.Currency.getInstance(money.currencyCode)
    val digits = currencyFractionDigits(money.currencyCode)
    val shown = displayFractionDigits(money)
    val format = NumberFormat.getCurrencyInstance(locale).apply {
        this.currency = currency
        minimumFractionDigits = shown
        maximumFractionDigits = shown
    }
    return format.format(BigDecimal.valueOf(money.amountMinor, digits))
}

/** The ISO-4217 currency code of [locale], or [FALLBACK_CURRENCY_CODE]. */
fun defaultCurrencyCode(locale: Locale = Locale.getDefault()): String =
    runCatching { Currency.getInstance(locale).currencyCode }.getOrDefault(FALLBACK_CURRENCY_CODE)
