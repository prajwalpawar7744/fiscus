package com.prajwalpawar.fiscus.ui.feature.settings

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.ui.components.ShapeAccentIcon
import com.prajwalpawar.fiscus.ui.components.containedSegmentedColors
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Stateless Settings screen (ADR-040): one contained segmented group of rows that open the
 * Accounts and Categories pages. No state yet; all actions go out through lambdas.
 */
@OptIn(ExperimentalMaterial3Api::class) // TopAppBarDefaults scroll behavior
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenCategories: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.nav_settings)) },
                navigationIcon = {
                    IconButton(shapes = IconButtonDefaults.shapes(), onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_rounded),
                            contentDescription = stringResource(R.string.action_navigate_back),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = FiscusLayout.MaxContentWidth)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(FiscusLayout.ScreenMargin),
                verticalArrangement = Arrangement.spacedBy(ListItemDefaults.SegmentedGap),
            ) {
                SettingsRow(
                    icon = R.drawable.ic_account_balance_wallet_rounded,
                    title = stringResource(R.string.accounts_title),
                    supportingText = stringResource(R.string.settings_accounts_supporting),
                    index = 0,
                    onClick = onOpenAccounts,
                )
                SettingsRow(
                    icon = R.drawable.ic_category_rounded,
                    title = stringResource(R.string.settings_categories),
                    supportingText = stringResource(R.string.settings_categories_supporting),
                    index = 1,
                    onClick = onOpenCategories,
                )
            }
        }
    }
}

private const val SettingsRowCount = 2

/** One row of the settings group: icon accent, title and what it manages. */
@Composable
private fun SettingsRow(
    @DrawableRes icon: Int,
    title: String,
    supportingText: String,
    index: Int,
    onClick: () -> Unit,
) {
    SegmentedListItem(
        onClick = onClick,
        shapes = ListItemDefaults.segmentedShapes(index, SettingsRowCount),
        colors = containedSegmentedColors(),
        leadingContent = { ShapeAccentIcon(icon = icon) },
        supportingContent = { Text(supportingText) },
    ) {
        Text(title)
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun SettingsScreenPreview() {
    FiscusTheme(dynamicColor = false) {
        SettingsScreen(
            onBack = {},
            onOpenAccounts = {},
            onOpenCategories = {},
        )
    }
}
