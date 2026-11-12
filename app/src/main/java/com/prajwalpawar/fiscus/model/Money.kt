package com.prajwalpawar.fiscus.model

/**
 * An amount of money in minor units (paise, cents) of [currencyCode] (ISO-4217).
 * Never use Float/Double for money (ADR-003).
 */
data class Money(val amountMinor: Long, val currencyCode: String) {

    val isZero: Boolean get() = amountMinor == 0L
    val isNegative: Boolean get() = amountMinor < 0L

    operator fun plus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amountMinor = Math.addExact(amountMinor, other.amountMinor))
    }

    operator fun minus(other: Money): Money {
        requireSameCurrency(other)
        return copy(amountMinor = Math.subtractExact(amountMinor, other.amountMinor))
    }

    operator fun unaryMinus(): Money = copy(amountMinor = Math.negateExact(amountMinor))

    private fun requireSameCurrency(other: Money) = require(currencyCode == other.currencyCode) {
        "Currency mismatch: $currencyCode vs ${other.currencyCode}"
    }

    companion object {
        fun zero(currencyCode: String) = Money(0L, currencyCode)
    }
}
