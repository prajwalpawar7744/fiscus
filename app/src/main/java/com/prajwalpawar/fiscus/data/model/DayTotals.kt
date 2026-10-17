package com.prajwalpawar.fiscus.data.model

import java.time.LocalDate

// Income and expense for one day, in hundredths.
data class DayTotals(
    val day: LocalDate,
    val income: Long,
    val expense: Long
)
