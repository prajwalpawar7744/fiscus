package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers

/**
 * Shared component: one labelled fact on a detail page (ADR-025/026), a contained
 * [SegmentedListItem] with an icon, the [label] as overline and the [value] in `titleMedium`
 * (tabular, so amounts line up). Pass [shapes] from `ListItemDefaults.segmentedShapes`.
 */
@Composable
fun DetailRow(
    @DrawableRes icon: Int,
    label: String,
    value: String,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
) {
    SegmentedListItem(
        shapes = shapes,
        modifier = modifier,
        colors = containedSegmentedColors(),
        leadingContent = { Icon(painterResource(icon), contentDescription = null) },
        overlineContent = { Text(label) },
    ) {
        Text(text = value, style = MaterialTheme.typography.titleMedium.tabularNumbers)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun DetailRowPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            Column(
                modifier = Modifier.padding(FiscusLayout.ScreenMargin),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                DetailRow(
                    R.drawable.ic_payments_rounded,
                    "Account",
                    "Cash",
                    ListItemDefaults.segmentedShapes(0, 2),
                )
                DetailRow(
                    R.drawable.ic_calendar_today_rounded,
                    "Date",
                    "15 Nov 2026, 4:00 pm",
                    ListItemDefaults.segmentedShapes(1, 2),
                )
            }
        }
    }
}
