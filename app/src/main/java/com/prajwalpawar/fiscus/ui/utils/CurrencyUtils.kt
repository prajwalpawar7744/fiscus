package com.prajwalpawar.fiscus.ui.utils

import android.annotation.SuppressLint
import java.text.NumberFormat
import java.util.*

@SuppressLint("DefaultLocale")
fun formatCurrency(amount: Double, currencyCode: String, isMasked: Boolean = false): String {
    if (isMasked) {
        val symbol = try {
            val locale = when (currencyCode) {
                "INR" -> Locale.forLanguageTag("en-IN")
                "USD" -> Locale.US
                "EUR" -> Locale.FRANCE
                "GBP" -> Locale.UK
                "JPY" -> Locale.JAPAN
                else -> Locale.getDefault()
            }
            Currency.getInstance(currencyCode).getSymbol(locale)
        } catch (e: Exception) {
            currencyCode
        }
        return "$symbol ••••"
    }

    val locale = when (currencyCode) {
        "INR" -> Locale.forLanguageTag("en-IN")
        "USD" -> Locale.US
        "EUR" -> Locale.FRANCE
        "GBP" -> Locale.UK
        "JPY" -> Locale.JAPAN
        else -> Locale.getDefault()
    }
    
    return try {
        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = Currency.getInstance(currencyCode)
        format.format(amount)
    } catch (e: Exception) {
        // Fallback to simple formatting if currency code or locale is invalid
        "$currencyCode ${String.format("%.2f", amount)}"
    }
}
