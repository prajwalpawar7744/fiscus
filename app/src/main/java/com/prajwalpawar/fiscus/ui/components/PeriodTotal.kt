package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Shared component: one period total ("Income ₹50,000") with its type's arrow in a
 * [ShapeAccentIcon] in the type's accent roles (ADR-023). Used by the Transactions summary card
 * and the Home hero (ADR-033). The label is `labelMedium` in [labelColor]: the default
 * `onSurfaceVariant` only on surface-family containers; on any other container pass its own
 * on-color (DESIGN_SYSTEM.md contrast rule). TalkBack reads "Income, ₹50,000".
 */
@Composable
fun PeriodTotal(
    type: TransactionType,
    amount: Money,
    modifier: Modifier = Modifier,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    val label = stringResource(type.labelRes)
    val roles = type.accentRoles()
    val formatted = formatMoney(amount)
    Row(
        modifier = modifier.clearAndSetSemantics { contentDescription = "$label, $formatted" },
        horizontalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ShapeAccentIcon(
            icon = type.iconRes,
            containerColor = roles.container,
            contentColor = roles.content,
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor,
            )
            AmountText(text = formatted, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun PeriodTotalPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            Column(
                modifier = Modifier.padding(FiscusLayout.ScreenMargin),
                verticalArrangement = Arrangement.spacedBy(FiscusLayout.ItemSpacing),
            ) {
                PeriodTotal(TransactionType.INCOME, Money(5_000_000, "INR"))
                PeriodTotal(TransactionType.EXPENSE, Money(1_245_000, "INR"))
            }
        }
    }
}
