@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.prajwalpawar.fiscus.ui.components

import com.prajwalpawar.fiscus.ui.components.FiscusTooltip
import androidx.compose.ui.Alignment
import com.prajwalpawar.fiscus.ui.theme.FiscusReducedMotion
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.expandVertically
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
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
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// A read-only date field that opens a docked single-date picker below (or above) it.
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
                FiscusTooltip("Select $label") {
                    IconButton(
                        shapes = IconButtonDefaults.shapes(),
                        enabled = enabled,
                        onClick = {
                            pickerVisibility.targetState = !pickerVisibility.targetState
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.DateRange,
                            contentDescription = "Select $label"
                        )
                    }
                }
            }
        )

        DockedPickerPopup(
            visibility = pickerVisibility,
            onDismissRequest = { pickerVisibility.targetState = false }
        ) {
            key(selectedDateMillis) {
                val datePickerState = rememberDatePickerState(
                    initialSelectedDateMillis = selectedDateMillis
                )

                DatePicker(
                    state = datePickerState,
                    showModeToggle = false,
                    modifier = Modifier.fillMaxWidth(),
                    // to get rid of the headline
                    title = null,
                    headline = null,
                )

                DockedPickerActions(
                    onCancel = { pickerVisibility.targetState = false },
                    confirmEnabled = datePickerState.selectedDateMillis != null,
                    onConfirm = {
                        datePickerState.selectedDateMillis?.let(onDateSelected)
                        pickerVisibility.targetState = false
                    }
                )
            }
        }
    }
}

// The docked picker surface shared by the date and date-range pickers: anchored to the
// composable it's placed in, opening below it (or above when there's no room), with an
// Expressive scale-and-fade in and out. Content stays composed while it animates out.
@Composable
internal fun DockedPickerPopup(
    visibility: MutableTransitionState<Boolean>,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    if (!visibility.currentState && !visibility.targetState) return

    val motionScheme = MaterialTheme.motionScheme
    val popupGap = with(LocalDensity.current) {
        FiscusSpacing.xs.roundToPx()
    }

    val reducedMotion = FiscusReducedMotion.enabled

    Popup(
        popupPositionProvider = DockedPickerPositionProvider(popupGap),
        onDismissRequest = onDismissRequest
    ) {
        AnimatedVisibility(
            visibleState = visibility,
            // M3 enter/exit: a popup under its field expands downward along one axis;
            // no scaling (it implies an elevation change). Reduced motion: fade only.
            enter = if (reducedMotion) {
                fadeIn(motionScheme.fastEffectsSpec())
            } else {
                expandVertically(motionScheme.defaultSpatialSpec(), expandFrom = Alignment.Top) +
                    fadeIn(motionScheme.fastEffectsSpec())
            },
            exit = if (reducedMotion) {
                fadeOut(motionScheme.fastEffectsSpec())
            } else {
                shrinkVertically(motionScheme.fastSpatialSpec(), shrinkTowards = Alignment.Top) +
                    fadeOut(motionScheme.fastEffectsSpec())
            }
        ) {
            Surface(
                modifier = Modifier
                    .widthIn(max = FiscusSize.datePickerMaxWidth),
                shape = MaterialTheme.shapes.extraLarge,
                // Matches the pickers' own container so the two read as one surface.
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shadowElevation = FiscusElevation.level2
            ) {
                // No side padding: the pickers pad themselves and need their full width.
                Column(
                    modifier = Modifier.padding(
                        top = FiscusSpacing.sm,
                        bottom = FiscusSpacing.sm
                    ),
                    verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm),
                    content = content
                )
            }
        }
    }
}

// Cancel / Confirm along the bottom, with an optional action on the left (e.g. a reset).
@Composable
internal fun DockedPickerActions(
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean,
    leadingAction: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = FiscusSpacing.sm),
        horizontalArrangement = Arrangement.End
    ) {
        leadingAction?.invoke()
        Spacer(Modifier.weight(1f))

        TextButton(onClick = onCancel, shapes = ButtonDefaults.shapes()) {
            Text("Cancel")
        }

        TextButton(

            shapes = ButtonDefaults.shapes(),
            enabled = confirmEnabled,
            onClick = onConfirm
        ) {
            Text("Confirm")
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

// To position the picker above or below depending on the space
private class DockedPickerPositionProvider(
    private val gapInPx: Int
) : PopupPositionProvider {
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

        // Centred under a narrow anchor (a label), aligned to a wide one (a field).
        val x = if (anchorBounds.width < popupContentSize.width) {
            anchorBounds.center.x - popupContentSize.width / 2
        } else {
            when (layoutDirection) {
                LayoutDirection.Ltr -> anchorBounds.left
                LayoutDirection.Rtl -> anchorBounds.right - popupContentSize.width
            }
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
