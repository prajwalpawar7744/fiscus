package com.prajwalpawar.fiscus.data.model

import java.math.BigDecimal
import java.math.RoundingMode

// A line on an itemised transaction: quantity × unit price.
data class TransactionItem(
    val name: String,
    // Hundredths (1.5 → 150), so weights and part quantities work.
    val quantity: Long,
    // Hundredths of the currency unit.
    val unitPrice: Long
) {
    // Hundredths of the currency unit, rounded half-up to the nearest hundredth.
    val total: Long
        get() = BigDecimal.valueOf(quantity)
            .multiply(BigDecimal.valueOf(unitPrice))
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP)
            .longValueExact()
}
