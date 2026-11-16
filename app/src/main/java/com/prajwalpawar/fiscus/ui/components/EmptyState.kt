package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Shared component: **every page's empty state** (ADR-031): nothing yet, nothing matches, search
 * hint, and placeholder pages. A large decorative [ShapeAccentIcon] (M3_EXPRESSIVE §3: expressive
 * shapes for empty states, not data) above a centered message, with an optional [action] (e.g. a
 * "Clear filters" `TextButton`) below. The icon is decorative; the message carries the meaning.
 */
@Composable
fun EmptyState(
    @DrawableRes icon: Int,
    message: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
) {
    // Fills the space it's given and centers its content in it (ADR-031). In a lazy list, pass
    // `Modifier.fillParentMaxSize()` so it gets the list's height.
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(vertical = FiscusLayout.SectionSpacing),
        verticalArrangement = Arrangement.spacedBy(
            FiscusLayout.ItemSpacing,
            Alignment.CenterVertically,
        ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ShapeAccentIcon(
            icon = icon,
            size = FiscusLayout.LargeAccentSize,
            iconSize = FiscusLayout.LargeAccentIconSize,
            modifier = Modifier.padding(bottom = FiscusLayout.ItemSpacing),
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        action?.invoke()
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun EmptyStatePreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            EmptyState(
                icon = R.drawable.ic_search_rounded,
                message = "No transactions match these filters.",
                action = { TextButton(shapes = ButtonDefaults.shapes(), onClick = {}) { Text("Clear filters") } },
            )
        }
    }
}
