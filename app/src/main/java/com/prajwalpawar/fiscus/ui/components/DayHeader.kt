package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import kotlinx.datetime.LocalDate

/**
 * Shared component: the header above one day of transactions (ADR-022/023): [dayLabel] with
 * "Today" emphasized (M3 tactic 3), and the day's [net] after its direction arrow (ADR-032),
 * TalkBack reading the signed net. A heading for TalkBack.
 */
@Composable
fun DayHeader(date: LocalDate, today: LocalDate, net: Money?, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = FiscusLayout.ScreenMargin,
                end = FiscusLayout.ScreenMargin,
                top = FiscusLayout.ScreenMargin,
                bottom = FiscusLayout.TightSpacing,
            )
            .semantics(mergeDescendants = true) { heading() },
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // The current day is the key group: emphasized type (M3 tactic 3), the rest stays regular.
        val isToday = date == today
        Text(
            text = dayLabel(date, today),
            style = if (isToday) {
                MaterialTheme.typography.titleMediumEmphasized
            } else {
                MaterialTheme.typography.titleSmall
            },
            color = if (isToday) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )
        net?.let {
            val netText = signedNetText(it)
            AmountText(
                text = netAmountText(it),
                style = MaterialTheme.typography.labelLarge,
                icon = netIconRes(it),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.clearAndSetSemantics { contentDescription = netText },
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun DayHeaderPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            Column {
                val today = LocalDate(2026, 11, 15)
                DayHeader(today, today, Money(-45_000, "INR"))
                DayHeader(LocalDate(2026, 11, 14), today, Money(5_000_000, "INR"))
            }
        }
    }
}
