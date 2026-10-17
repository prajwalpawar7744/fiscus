package com.prajwalpawar.fiscus.util

import android.icu.text.NumberFormat
import java.math.BigDecimal
import java.util.Currency
import java.util.Locale

// Amounts are stored in hundredths, so always show two decimals.
private const val STORED_DECIMALS = 2

// "1250" -> "₹12.50" in an Indian locale, "$12.50" in a US one.
// ICU rather than java.text, which can't do Indian grouping (₹12,34,567.89).
fun formatMoney(hundredths: Long, locale: Locale = Locale.getDefault()): String {
    val format = NumberFormat.getCurrencyInstance(locale).apply {
        minimumFractionDigits = STORED_DECIMALS
        maximumFractionDigits = STORED_DECIMALS
    }

    return format.format(BigDecimal.valueOf(hundredths, STORED_DECIMALS))
}

// Locales without a country (e.g. plain "en") have no currency, so fall back to the generic sign.
fun currencySymbol(locale: Locale = Locale.getDefault()): String {
    return runCatching {
        Currency.getInstance(locale).getSymbol(locale)
    }.getOrDefault("¤")
}
