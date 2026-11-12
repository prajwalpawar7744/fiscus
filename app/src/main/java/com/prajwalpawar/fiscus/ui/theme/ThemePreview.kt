package com.prajwalpawar.fiscus.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Preview-only catalog for checking the theme. Not used in the app.

@OptIn(ExperimentalMaterial3ExpressiveApi::class) // LoadingIndicator, ButtonGroup
@Composable
private fun ThemeCatalog() {
    val colors = MaterialTheme.colorScheme
    Surface(color = colors.surface) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ElevatedCard {
                Column(Modifier.padding(16.dp)) {
                    Text("Total balance", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "₹1,24,580.00",
                        style = MaterialTheme.typography.displayMediumEmphasized.tabularNumbers,
                    )
                }
            }
            // Related actions use ButtonGroup, never a plain Row of buttons (ADR-013).
            ButtonGroup(
                overflowIndicator = { menuState ->
                    TextButton(onClick = { menuState.show() }) { Text("More") }
                },
            ) {
                clickableItem(onClick = {}, label = "Add")
                clickableItem(onClick = {}, label = "Filter")
                clickableItem(onClick = {}, label = "Export")
            }
            ListItem(
                supportingContent = { Text("Today") },
                trailingContent = {
                    Text("−₹840.00", style = MaterialTheme.typography.titleMedium.tabularNumbers)
                },
            ) {
                Text("Groceries")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LoadingIndicator()
                FloatingActionButton(onClick = {}) { Text("+") }
            }
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Swatch("primary", colors.primary, colors.onPrimary)
                Swatch("primaryContainer", colors.primaryContainer, colors.onPrimaryContainer)
                Swatch("secondary", colors.secondary, colors.onSecondary)
                Swatch("secondaryContainer", colors.secondaryContainer, colors.onSecondaryContainer)
                Swatch("tertiary", colors.tertiary, colors.onTertiary)
                Swatch("tertiaryContainer", colors.tertiaryContainer, colors.onTertiaryContainer)
                Swatch("surfaceContainer", colors.surfaceContainer, colors.onSurface)
                Swatch("error", colors.error, colors.onError)
            }
        }
    }
}

@Composable
private fun Swatch(name: String, color: Color, onColor: Color) {
    Surface(color = color, contentColor = onColor, shape = MaterialTheme.shapes.medium) {
        Text(name, Modifier.padding(8.dp).size(width = 120.dp, height = 20.dp))
    }
}

@Preview(name = "Brand light", showBackground = true)
@Composable
private fun BrandLightPreview() = FiscusTheme(darkTheme = false, dynamicColor = false) { ThemeCatalog() }

@Preview(name = "Brand dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun BrandDarkPreview() = FiscusTheme(darkTheme = true, dynamicColor = false) { ThemeCatalog() }

@Preview(name = "Brand light, large font", showBackground = true, fontScale = 2f)
@Composable
private fun BrandLargeFontPreview() = FiscusTheme(darkTheme = false, dynamicColor = false) { ThemeCatalog() }

@Preview(name = "Dynamic light", showBackground = true)
@Composable
private fun DynamicLightPreview() = FiscusTheme(darkTheme = false, dynamicColor = true) { ThemeCatalog() }
