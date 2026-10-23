@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.prajwalpawar.fiscus.ui.components

import com.prajwalpawar.fiscus.ui.components.FiscusTooltip
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.Period
import java.time.YearMonth

// ‹ October 2026 ▾ › — the arrows step by the period's own length (a month, a day, or a
// range), never past today; tapping the label opens a docked range picker beneath it.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FiscusPeriodSwitcher(
    period: Period,
    onPeriodChange: (Period) -> Unit,
    modifier: Modifier = Modifier
) {
    val pickerVisibility = remember { MutableTransitionState(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FiscusSpacing.xs, vertical = FiscusSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FiscusTooltip("Previous period") {
            IconButton(onClick = { onPeriodChange(period.previous()) }, shapes = IconButtonDefaults.shapes()) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                    contentDescription = "Previous period"
                )
            }
        }

        // The picker anchors to this box, so it opens centred under the label.
        Box(
            modifier = Modifier.weight(1f),
            contentAlignment = Alignment.Center
        ) {
            TextButton(
                onClick = { pickerVisibility.targetState = true },
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = period.label(),
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(FiscusSpacing.xs))
                Icon(
                    imageVector = Icons.Rounded.ArrowDropDown,
                    contentDescription = "Choose dates"
                )
            }

            DockedDateRangePicker(
                visibility = pickerVisibility,
                period = period,
                onPeriodSelected = onPeriodChange,
                resetLabel = "This month",
                onReset = { onPeriodChange(Period.currentMonth()) }
            )
        }

        FiscusTooltip("Next period") {
            IconButton(

                shapes = IconButtonDefaults.shapes(),
                onClick = { onPeriodChange(period.next()) },
                enabled = period.canGoForward()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                    contentDescription = "Next period"
                )
            }
        }
    }
}

// "October 2026" in the device language.
fun monthLabel(month: YearMonth): String = Period.month(month).label()
