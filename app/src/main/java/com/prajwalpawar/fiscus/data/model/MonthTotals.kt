package com.prajwalpawar.fiscus.data.model

import java.time.YearMonth

// Income and expense for one calendar month, in hundredths.
data class MonthTotals(
    val month: YearMonth,
    val income: Long,
    val expense: Long
)
