package com.prajwalpawar.fiscus.data.model

// Income and expense over a period, in hundredths; transfers are not in the sums.
data class PeriodTotals(
    val income: Long = 0L,
    val expense: Long = 0L,
    // Every transaction in the period, transfers included.
    val count: Int = 0
)
