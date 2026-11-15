package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers

/**
 * Shared component: an amount on **one line**, never wrapped ("₹40,000.00" must not break). It
 * uses [style] (a type-scale token) as its size and only steps down when the amount doesn't fit
 * (`TextAutoSize.StepBased` capped at the style's own size, ADR-027). Always tabular figures.
 */
@Composable
fun AmountText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        style = style.tabularNumbers,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        autoSize = TextAutoSize.StepBased(maxFontSize = style.fontSize),
        modifier = modifier,
    )
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun AmountTextPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            Column(modifier = Modifier.padding(FiscusLayout.ScreenMargin)) {
                AmountText("−₹450.00", MaterialTheme.typography.displayLargeEmphasized)
                // Narrow on purpose: the big amount shrinks instead of wrapping.
                AmountText(
                    text = "−₹40,00,000.00",
                    style = MaterialTheme.typography.displayLargeEmphasized,
                    modifier = Modifier.width(FiscusLayout.MaxContentWidth / 3),
                )
            }
        }
    }
}
