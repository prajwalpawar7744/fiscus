package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

// ‹ October 2026 › — steps through months; never past the current one.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FiscusMonthSwitcher(
    month: YearMonth,
    canGoForward: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FiscusSpacing.xs, vertical = FiscusSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "Previous month"
            )
        }

        Text(
            text = monthLabel(month),
            style = MaterialTheme.typography.titleLargeEmphasized,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )

        IconButton(onClick = onNext, enabled = canGoForward) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Next month"
            )
        }
    }
}

// "October 2026" in the device language.
fun monthLabel(month: YearMonth): String {
    return month.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale.getDefault()))
}
