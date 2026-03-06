package com.prajwalpawar.budgetear.ui.utils

import java.text.NumberFormat
import java.util.*

fun formatCurrency(amount: Double, currencyCode: String): String {
    val locale = when (currencyCode) {
        "USD" -> Locale.US
        "EUR" -> Locale.FRANCE
        "GBP" -> Locale.UK
        "INR" -> Locale("en", "IN")
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
