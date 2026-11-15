package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.text.appendInlineContent
import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.em
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers

/**
 * Shared component: an amount on **one line**, never wrapped ("₹40,000.00" must not break). It
 * uses [style] (a type-scale token) as its size and only steps down when the amount doesn't fit
 * (`TextAutoSize.StepBased` capped at the style's own size, ADR-027). Always tabular figures.
 * An optional [icon] (a type's direction arrow, ADR-032) sits inline before the amount at the
 * text's own size, so it steps down with it; it's decorative, tinted by the content color.
 */
@Composable
fun AmountText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
    @DrawableRes icon: Int? = null,
    color: Color = Color.Unspecified,
) {
    Text(
        text = buildAnnotatedString {
            if (icon != null) {
                appendInlineContent(ICON_ID)
                append(' ')
            }
            append(text)
        },
        inlineContent = icon?.let { res ->
            mapOf(
                ICON_ID to InlineTextContent(
                    Placeholder(1.em, 1.em, PlaceholderVerticalAlign.TextCenter),
                ) {
                    Icon(painter = painterResource(res), contentDescription = null)
                },
            )
        } ?: emptyMap(),
        style = style.tabularNumbers,
        textAlign = textAlign,
        color = color,
        maxLines = 1,
        softWrap = false,
        autoSize = TextAutoSize.StepBased(maxFontSize = style.fontSize),
        modifier = modifier,
    )
}

private const val ICON_ID = "icon"

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun AmountTextPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            Column(modifier = Modifier.padding(FiscusLayout.ScreenMargin)) {
                AmountText(
                    text = "₹450.00",
                    style = MaterialTheme.typography.displayLargeEmphasized,
                    icon = R.drawable.ic_arrow_downward_rounded,
                )
                // Narrow on purpose: the big amount shrinks instead of wrapping.
                AmountText(
                    text = "₹40,00,000.00",
                    style = MaterialTheme.typography.displayLargeEmphasized,
                    modifier = Modifier.width(FiscusLayout.MaxContentWidth / 3),
                )
            }
        }
    }
}
