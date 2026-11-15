package com.prajwalpawar.fiscus.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.prajwalpawar.fiscus.R
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * A day as people say it: "Today", "Yesterday", otherwise the localized full date
 * ("Saturday, 14 November 2026"). [today] comes from the injected clock (ADR-018), never from
 * `Clock.System` here.
 */
@Composable
fun dayLabel(date: LocalDate, today: LocalDate): String = when (date) {
    today -> stringResource(R.string.day_today)
    today.minus(DatePeriod(days = 1)) -> stringResource(R.string.day_yesterday)
    else -> remember(date) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(date.toJavaLocalDate())
    }
}
