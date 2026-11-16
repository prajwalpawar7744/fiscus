package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toLocalDateTime

/**
 * Shared component: a read-only date field with an M3 **docked** date picker shown below it (the
 * Compose docs' docked pattern: [DatePicker] in a [Popup] anchored to the field). The picker
 * expands from the field on the theme's spatial spring and fades in on its effects spring, and
 * closes once a different date is picked.
 *
 * Use it wherever a single date is chosen inside a form (DESIGN_SYSTEM.md "Shared components").
 * [DatePicker] works in UTC-midnight millis; conversion happens here, so callers only see
 * [LocalDate].
 */
@OptIn(ExperimentalMaterial3Api::class) // DatePicker state
@Composable
fun DockedDatePickerField(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val motion = MaterialTheme.motionScheme
    var showPicker by rememberSaveable { mutableStateOf(false) }
    var fieldHeight by remember { mutableIntStateOf(0) }
    val formatted = remember(date) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).format(date.toJavaLocalDate())
    }

    Box(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = formatted,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                IconButton(shapes = IconButtonDefaults.shapes(), onClick = { showPicker = !showPicker }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_calendar_today_rounded),
                        contentDescription = stringResource(R.string.action_choose_date),
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { fieldHeight = it.height },
        )
        if (showPicker) {
            val pickerState = rememberDatePickerState(
                initialSelectedDateMillis = date.atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds(),
            )
            LaunchedEffect(pickerState.selectedDateMillis) {
                val millis = pickerState.selectedDateMillis ?: return@LaunchedEffect
                val picked = Instant.fromEpochMilliseconds(millis).toLocalDateTime(TimeZone.UTC).date
                if (picked != date) {
                    onDateSelected(picked)
                    showPicker = false
                }
            }
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, fieldHeight),
                onDismissRequest = { showPicker = false },
                properties = PopupProperties(focusable = true),
            ) {
                val visibility = remember { MutableTransitionState(false) }.apply { targetState = true }
                AnimatedVisibility(
                    visibleState = visibility,
                    enter = expandVertically(motion.defaultSpatialSpec(), expandFrom = Alignment.Top) +
                        fadeIn(motion.defaultEffectsSpec()),
                ) {
                    Surface(
                        shape = DatePickerDefaults.shape,
                        // The picker's own container color and elevation token (no app-made values).
                        color = DatePickerDefaults.colors().containerColor,
                        tonalElevation = DatePickerDefaults.TonalElevation,
                    ) {
                        DatePicker(
                            state = pickerState,
                            showModeToggle = false,
                            title = null,
                            headline = null,
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun DockedDatePickerFieldPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            DockedDatePickerField(
                date = LocalDate(2026, 11, 15),
                onDateSelected = {},
                label = "Date",
                modifier = Modifier.padding(FiscusLayout.ScreenMargin),
            )
        }
    }
}
