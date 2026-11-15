package com.prajwalpawar.fiscus.ui.feature.home

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.components.AddTransactionFabMenu
import com.prajwalpawar.fiscus.ui.components.LocalSnackbarHostState
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme
import com.prajwalpawar.fiscus.ui.theme.tabularNumbers


/** Stateless Home screen. All state comes from [uiState]; all actions go out through lambdas. */
@OptIn(ExperimentalMaterial3Api::class) // TopAppBarDefaults scroll behavior
@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onOpenSettings: () -> Unit,
    onAddTransaction: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text(stringResource(R.string.nav_home)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            painter = painterResource(R.drawable.ic_settings_rounded),
                            contentDescription = stringResource(R.string.nav_settings),
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(LocalSnackbarHostState.current) },
        floatingActionButton = { AddTransactionFabMenu(onAddTransaction = onAddTransaction) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(FiscusLayout.ScreenMargin),
            verticalArrangement = Arrangement.spacedBy(FiscusLayout.ScreenMargin),
        ) {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(FiscusLayout.ScreenMargin)) {
                    Text(
                        text = stringResource(R.string.home_balance_label),
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = stringResource(uiState.balance),
                        style = MaterialTheme.typography.displayMediumEmphasized.tabularNumbers,
                    )
                }
            }
            Text(
                text = stringResource(uiState.emptyMessage),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun HomeScreenPreview() {
    FiscusTheme(dynamicColor = false) {
        HomeScreen(
            uiState = HomeUiState(),
            onOpenSettings = {},
            onAddTransaction = {},
        )
    }
}
