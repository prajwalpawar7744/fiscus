package com.prajwalpawar.fiscus.ui.utils

import android.annotation.SuppressLint
import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

@SuppressLint("DefaultLocale")
fun formatCurrency(
    amount: Double,
    currencyCode: String,
    isMasked: Boolean = false,
    isCompact: Boolean = false
): String {
    if (isMasked) {
        val symbol = try {
            val locale = getLocaleForCurrency(currencyCode)
            Currency.getInstance(currencyCode).getSymbol(locale)
        } catch (e: Exception) {
            currencyCode
        }
        return "$symbol ••••"
    }

    val locale = getLocaleForCurrency(currencyCode)

    return try {
        val symbol = Currency.getInstance(currencyCode).getSymbol(locale)
        val formattedAmount = if (isCompact) {
            formatCompactAmount(amount)
        } else {
            val format = NumberFormat.getCurrencyInstance(locale)
            format.currency = Currency.getInstance(currencyCode)
            // Remove the currency symbol from the formatted string because we prepend it
            // This is a bit hacky but getCurrencyInstance doesn't always behave consistently with just the number
            val currencyFormat = format.format(amount)
            return currencyFormat
        }

        // For compact mode, we manually construct the string with the symbol
        "$symbol$formattedAmount"
    } catch (e: Exception) {
        // Fallback to simple formatting if currency code or locale is invalid
        "$currencyCode ${String.format("%.2f", amount)}"
    }
}

private fun getLocaleForCurrency(currencyCode: String): Locale {
    return when (currencyCode) {
        "INR" -> Locale.forLanguageTag("en-IN")
        "USD" -> Locale.US
        "EUR" -> Locale.FRANCE
        "GBP" -> Locale.UK
        "JPY" -> Locale.JAPAN
        else -> Locale.getDefault()
    }
}

private fun formatCompactAmount(amount: Double): String {
    val absAmount = Math.abs(amount)
    return when {
        absAmount >= 1_000_000_000 -> String.format("%.1fB", amount / 1_000_000_000.0)
        absAmount >= 1_000_000 -> String.format("%.1fM", amount / 1_000_000.0)
        absAmount >= 1_000 -> String.format("%.1fk", amount / 1_000.0)
        else -> String.format("%.2f", amount)
    }
}
