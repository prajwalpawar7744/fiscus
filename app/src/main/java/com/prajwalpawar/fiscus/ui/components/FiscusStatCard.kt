package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

// One stat in a collection of tiles (Home's Income/Spent, Analysis's headline numbers).
// M3: cards shown together share one type, shape and layout, so every stat tile is this
// filled card: the icon in a badge of the user's icon shape (M3E: shape draws the eye), then
// the label, an emphasized value that shrinks to fit, and an optional caption.
// The hero (total balance) and carousel cards are deliberately not stat cards.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FiscusStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    caption: String? = null,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer
    ),
    // Label and caption colour: onSurfaceVariant on a surface container; on a coloured
    // container pass its on colour (M3 colour roles in pairs).
    supportingColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    // The badge's pair; on a coloured container use the inverse (its on colour as the badge).
    iconContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    iconColor: Color = MaterialTheme.colorScheme.onPrimaryContainer
) {
    // TalkBack reads the tile as one item: "Spent, ₹1,200, Income ₹3,000".
    Card(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = MaterialTheme.shapes.large,
        colors = colors
    ) {
        Column(
            modifier = Modifier.padding(FiscusSpacing.md),
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)
        ) {
            FiscusListIcon(
                icon = icon,
                containerColor = iconContainerColor,
                contentColor = iconColor,
                modifier = Modifier.padding(bottom = FiscusSpacing.sm)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = supportingColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            val valueStyle = MaterialTheme.typography.headlineSmallEmphasized

            Text(
                text = value,
                style = valueStyle,
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(maxFontSize = valueStyle.fontSize)
            )

            if (caption != null) {
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = supportingColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
