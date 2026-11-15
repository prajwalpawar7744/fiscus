package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.model.TransactionType
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/**
 * Quick-add FAB menu (hero moment 2, ADR-010): one item per [TransactionType]. Collapses on Back
 * and after a choice. Shown on Home and Transactions.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class) // animateIcon
@Composable
fun AddTransactionFabMenu(
    onAddTransaction: (TransactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = expanded) { expanded = false }

    val menuDescription = stringResource(R.string.action_add_transaction)
    val stateDescription = stringResource(
        if (expanded) R.string.state_expanded else R.string.state_collapsed,
    )
    FloatingActionButtonMenu(
        modifier = modifier,
        expanded = expanded,
        button = {
            ToggleFloatingActionButton(
                modifier = Modifier.semantics {
                    contentDescription = menuDescription
                    this.stateDescription = stateDescription
                },
                checked = expanded,
                onCheckedChange = { expanded = it },
            ) {
                Icon(
                    painter = painterResource(
                        if (checkedProgress > 0.5f) R.drawable.ic_close_rounded else R.drawable.ic_add_rounded,
                    ),
                    contentDescription = null, // described by the button semantics
                    modifier = Modifier.animateIcon({ checkedProgress }),
                )
            }
        },
    ) {
        TransactionType.entries.forEach { type ->
            FloatingActionButtonMenuItem(
                onClick = {
                    expanded = false
                    onAddTransaction(type)
                },
                icon = { Icon(painterResource(type.iconRes), contentDescription = null) },
                text = { Text(stringResource(type.labelRes)) },
            )
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun AddTransactionFabMenuPreview() {
    FiscusTheme(dynamicColor = false) {
        AddTransactionFabMenu(onAddTransaction = {})
    }
}
