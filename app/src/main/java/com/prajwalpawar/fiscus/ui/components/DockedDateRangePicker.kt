@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.prajwalpawar.fiscus.ui.components

import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.util.Period
import java.time.LocalDate
import java.time.ZoneOffset

// Docked date-range picker for choosing a period, styled like DockedDatePicker. Material's
// DateRangePicker only lays months out as a vertical scrolling list (no single-month paging).
// Tapping one date twice picks that day alone. Future dates are disabled. Place it inside
// the composable it should anchor to.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockedDateRangePicker(
    visibility: MutableTransitionState<Boolean>,
    period: Period,
    onPeriodSelected: (Period) -> Unit,
    // Shown on the left as a quick way back, e.g. "This month".
    resetLabel: String? = null,
    onReset: (() -> Unit)? = null
) {
    val close = { visibility.targetState = false }

    DockedPickerPopup(
        visibility = visibility,
        onDismissRequest = close
    ) {
        key(period) {
            val todayMillis = remember {
                LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            }
            val range = period.toDateRange()
            val state = rememberDateRangePickerState(
                initialSelectedStartDateMillis = range.startMillis,
                // The picker's end is the last day itself, not the day after.
                initialSelectedEndDateMillis = range.endMillis - DAY_MILLIS,
                selectableDates = object : SelectableDates {
                    override fun isSelectableDate(utcTimeMillis: Long) =
                        utcTimeMillis <= todayMillis

                    override fun isSelectableYear(year: Int) = year <= LocalDate.now().year
                }
            )

            DateRangePicker(
                state = state,
                showModeToggle = false,
                // Bounded, so its scrolling months fit the popup.
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = FiscusSize.dateRangePickerMaxHeight)
                    .weight(1f, fill = false),
                // Same chrome as DockedDatePicker: no title or headline, just the calendar.
                title = null,
                headline = null
            )

            val start = state.selectedStartDateMillis
            val end = state.selectedEndDateMillis

            DockedPickerActions(
                onCancel = close,
                confirmEnabled = start != null,
                onConfirm = {
                    if (start != null) {
                        // A lone start date (no end yet) means that day by itself.
                        onPeriodSelected(Period.fromPickerMillis(start, end ?: start))
                    }
                    close()
                },
                leadingAction = if (resetLabel != null && onReset != null) {
                    {
                        TextButton(onClick = {
                            onReset()
                            close()
                        }, shapes = ButtonDefaults.shapes()) {
                            Text(resetLabel)
                        }
                    }
                } else {
                    null
                }
            )
        }
    }
}

private const val DAY_MILLIS = 86_400_000L
