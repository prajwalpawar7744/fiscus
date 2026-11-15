package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Shared component: an icon inside a [MaterialShapes] container, the app's one decorative shape
 * accent (M3_EXPRESSIVE.md §3: expressive shapes for accents such as category icons, never on data
 * containers). Always pass a matching color-role pair (e.g. `secondaryContainer` +
 * `onSecondaryContainer`, or `tertiary` + `onTertiary`). Decorative: the label next to it describes
 * it, so the icon has no content description.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class) // MaterialShapes
@Composable
fun ShapeAccentIcon(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer,
) {
    Surface(
        shape = MaterialShapes.Cookie9Sided.toShape(),
        color = containerColor,
        contentColor = contentColor,
        modifier = modifier.size(FiscusLayout.AccentSize),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(painterResource(icon), contentDescription = null)
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun ShapeAccentIconPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            Row(
                horizontalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
                modifier = Modifier.padding(FiscusLayout.ScreenMargin),
            ) {
                ShapeAccentIcon(icon = R.drawable.ic_restaurant_rounded)
                ShapeAccentIcon(
                    icon = R.drawable.ic_arrow_upward_rounded,
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                )
            }
        }
    }
}
