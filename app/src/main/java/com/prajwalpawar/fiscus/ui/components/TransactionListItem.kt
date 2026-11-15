package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.ListItemShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.core.money.formatMoney
import com.prajwalpawar.fiscus.model.Money
import com.prajwalpawar.fiscus.model.Transaction
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers
import kotlin.time.Instant

/**
 * Shared component: one transaction in a list, as an Expressive [SegmentedListItem] (ADR-022).
 * Pass [shapes] from `ListItemDefaults.segmentedShapes(index, count)` so items of one group join.
 *
 * The [icon] (the category's, or the type's for a transfer) sits in a [ShapeAccentIcon] in the
 * type's accent roles, matching the editor hero (ADR-023). The data stays calm (M3_EXPRESSIVE.md
 * §1): the title as headline, [supportingText] (e.g. "Food · Cash") and the signed amount in
 * tabular figures. Rows sit on `surfaceContainer` ([containedSegmentedColors]); the headline is
 * `titleMedium`, the amount follows its type's arrow (ADR-026).
 * TalkBack reads the type with the amount ("Expense, ₹450.00"), so the sign and
 * label carry the meaning, never color. In search results, pass [highlight] to emphasize the
 * matched part of the title.
 */
@Composable
fun TransactionListItem(
    transaction: Transaction,
    supportingText: String,
    @DrawableRes icon: Int,
    onClick: () -> Unit,
    shapes: ListItemShapes,
    modifier: Modifier = Modifier,
    highlight: String = "",
) {
    val amountDescription = stringResource(
        R.string.detail_amount_description,
        stringResource(transaction.type.labelRes),
        formatMoney(transaction.amount),
    )
    SegmentedListItem(
        onClick = onClick,
        shapes = shapes,
        modifier = modifier,
        colors = containedSegmentedColors(),
        leadingContent = {
            val roles = transaction.type.accentRoles()
            ShapeAccentIcon(
                icon = icon,
                containerColor = roles.container,
                contentColor = roles.content,
            )
        },
        supportingContent = {
            Text(supportingText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        trailingContent = {
            DirectionalAmount(
                type = transaction.type,
                amount = transaction.amount,
                modifier = Modifier.clearAndSetSemantics { contentDescription = amountDescription },
            )
        },
    ) {
        Text(
            text = highlightMatches(transaction.title, highlight),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The amount with the type's arrow before it (expense ↓ out, income ↑ in, transfer ⇄; the same
 * icons as everywhere else, `iconRes`) instead of a −/+ sign (ADR-026). The arrow's tint comes
 * from the content color (no colored icons); TalkBack reads "Expense, ₹450.00" from [modifier].
 */
@Composable
private fun DirectionalAmount(type: TransactionType, amount: Money, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FiscusLayout.TightSpacing),
    ) {
        Icon(
            painter = painterResource(type.iconRes),
            contentDescription = null,
            modifier = Modifier.size(FiscusLayout.InlineIconSize),
        )
        Text(
            text = formatMoney(amount),
            style = MaterialTheme.typography.titleSmall.tabularNumbers,
            maxLines = 1,
        )
    }
}

/**
 * [text] with every case-insensitive match of [query] in the emphasized weight of the headline
 * style (M3 tactic "guide attention with typography"; ADR-024). Weight only: no color change.
 */
@Composable
private fun highlightMatches(text: String, query: String): AnnotatedString {
    val needle = query.trim()
    if (needle.isEmpty()) return AnnotatedString(text)
    val weight = MaterialTheme.typography.bodyLargeEmphasized.fontWeight
    return buildAnnotatedString {
        append(text)
        var start = text.indexOf(needle, ignoreCase = true)
        while (start >= 0) {
            addStyle(SpanStyle(fontWeight = weight), start, start + needle.length)
            start = text.indexOf(needle, start + needle.length, ignoreCase = true)
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun TransactionListItemPreview() {
    val now = Instant.parse("2026-11-15T10:30:00Z")
    val lunch = Transaction(
        id = 1,
        type = TransactionType.EXPENSE,
        title = "Lunch with team",
        amount = Money(45_000, "INR"),
        accountId = 1,
        categoryId = 1,
        occurredAt = now,
        createdAt = now,
        updatedAt = now,
    )
    val salary = lunch.copy(
        id = 2,
        type = TransactionType.INCOME,
        title = "Salary",
        amount = Money(5_000_000, "INR"),
    )
    FiscusTheme(dynamicColor = false) {
        Surface {
            Column(
                modifier = Modifier.padding(FiscusLayout.ScreenMargin),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                TransactionListItem(
                    transaction = lunch,
                    supportingText = "Food & dining · Cash",
                    icon = R.drawable.ic_restaurant_rounded,
                    onClick = {},
                    shapes = ListItemDefaults.segmentedShapes(index = 0, count = 2),
                )
                TransactionListItem(
                    transaction = salary,
                    supportingText = "Salary · Bank",
                    icon = R.drawable.ic_work_rounded,
                    onClick = {},
                    shapes = ListItemDefaults.segmentedShapes(index = 1, count = 2),
                )
            }
        }
    }
}
