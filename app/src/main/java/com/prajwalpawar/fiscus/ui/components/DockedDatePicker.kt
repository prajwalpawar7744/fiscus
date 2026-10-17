package com.prajwalpawar.fiscus.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import com.prajwalpawar.fiscus.ui.theme.FiscusElevation
import com.prajwalpawar.fiscus.ui.theme.FiscusMotion
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DockedDatePicker(
    selectedDateMillis: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Date",
    placeholder: String = "Select date",
    enabled: Boolean = true,
) {
    // Kept as a transition state so the popup stays composed while it animates out.
    val pickerVisibility = remember { MutableTransitionState(false) }

    val popupGap = with(LocalDensity.current) {
        FiscusSpacing.xs.roundToPx()
    }

    // A read-only field never gets a click, so open the picker on any tap of the field.
    val fieldInteractionSource = remember { MutableInteractionSource() }

    LaunchedEffect(fieldInteractionSource, enabled) {
        fieldInteractionSource.interactions.collect { interaction ->
            if (enabled && interaction is PressInteraction.Release) {
                pickerVisibility.targetState = true
            }
        }
    }

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = selectedDateMillis?.let(::formatDate) ?: "",
            onValueChange = {},
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text(label)
            },
            placeholder = {
                Text(placeholder)
            },
            readOnly = true,
            singleLine = true,
            enabled = enabled,
            interactionSource = fieldInteractionSource,
            trailingIcon = {
                IconButton(
                    enabled = enabled,
                    onClick = {
                        pickerVisibility.targetState = !pickerVisibility.targetState
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select $label"
                    )
                }
            }
        )

        if (pickerVisibility.currentState || pickerVisibility.targetState) {
            key(selectedDateMillis) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDateMillis
                )

                val motionScheme = MaterialTheme.motionScheme

                Popup(
                    popupPositionProvider = DockedDatePickerPositionProvider(popupGap),
                    onDismissRequest = {
                        pickerVisibility.targetState = false
                    }
                ) {
                    AnimatedVisibility(
                        visibleState = pickerVisibility,
                        enter = fadeIn(motionScheme.defaultEffectsSpec()) +
                                scaleIn(
                                    animationSpec = motionScheme.defaultSpatialSpec(),
                                    initialScale = FiscusMotion.ENTER_SCALE
                                ),
                        exit = fadeOut(motionScheme.fastEffectsSpec()) +
                                scaleOut(
                                    animationSpec = motionScheme.fastSpatialSpec(),
                                    targetScale = FiscusMotion.ENTER_SCALE
                                )
                    ) {
                        Surface(
                            modifier = Modifier
                                .widthIn(max = FiscusSize.datePickerMaxWidth),
                            shape = MaterialTheme.shapes.extraLarge,
                            // Matches the DatePicker's own container so the two read as one surface.
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            shadowElevation = FiscusElevation.level2
                        ) {
                            // No side padding: DatePicker pads itself and needs its full width.
                            Column(
                                modifier = Modifier.padding(
                                    top = FiscusSpacing.sm,
                                    bottom = FiscusSpacing.sm
                                ),
                                verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
                            ) {
                                DatePicker(
                                    state = datePickerState,
                                    showModeToggle = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    // to get rid of the headline
                                    title = null,
                                    headline = null,
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = FiscusSpacing.sm),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(
                                        onClick = {
                                            pickerVisibility.targetState = false
                                        }
                                    ) {
                                        Text("Cancel")
                                    }

                                    TextButton(
                                        enabled = datePickerState.selectedDateMillis != null,
                                        onClick = {
                                            datePickerState.selectedDateMillis?.let(
                                                onDateSelected
                                            )
                                            pickerVisibility.targetState = false
                                        }
                                    ) {
                                        Text("Confirm")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(millis: Long): String {
    return SimpleDateFormat(
        "EEE, MMM d, yyyy",
        Locale.getDefault()
    ).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))
}

// To position the date picker above or below depending on the space
private class DockedDatePickerPositionProvider (
    private val gapInPx: Int
): PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize
    ): IntOffset {
        val spaceBelow = windowSize.height - anchorBounds.bottom
        val spaceAbove = anchorBounds.top

        val openAbove =
            spaceBelow < popupContentSize.height + gapInPx &&
                    spaceAbove > spaceBelow

        val y = if (openAbove) {
            anchorBounds.top - popupContentSize.height - gapInPx
        } else {
            anchorBounds.bottom + gapInPx
        }

        val x = when (layoutDirection) {
            LayoutDirection.Ltr -> anchorBounds.left
            LayoutDirection.Rtl ->
                anchorBounds.right - popupContentSize.width
        }.coerceIn(
            0,
            (windowSize.width - popupContentSize.width).coerceAtLeast(0)
        )

        return IntOffset(
            x = x,
            y = y.coerceIn(
                0,
                (windowSize.height - popupContentSize.height).coerceAtLeast(0)
            )
        )
    }
}
