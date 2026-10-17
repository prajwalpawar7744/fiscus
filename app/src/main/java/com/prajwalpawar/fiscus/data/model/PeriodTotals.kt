package com.prajwalpawar.fiscus.data.model

// Income and expense over a period, in hundredths; transfers are not included.
data class PeriodTotals(
    val income: Long = 0L,
    val expense: Long = 0L
)
