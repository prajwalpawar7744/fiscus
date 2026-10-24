package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedListItem
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.AccountType
import com.prajwalpawar.fiscus.ui.components.FiscusChipIcon
import com.prajwalpawar.fiscus.ui.components.FiscusListIcon
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.accountIcon
import com.prajwalpawar.fiscus.ui.components.accountTypeLabel
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import com.prajwalpawar.fiscus.util.currencySymbol

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AccountEditorScreen(
    viewModel: AccountEditorViewModel,
    onSaved: (id: Long) -> Unit,
    // After archiving, restoring or deleting, the editor closes.
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (!uiState.isLoaded) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        return
    }

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // A new account starts at the name; an existing one opens without the keyboard.
    LaunchedEffect(Unit) {
        if (!uiState.isEditing) focusRequester.requestFocus()
    }

    ItemEditorLayout(
        previewIcon = accountIcon(uiState.type),
        previewName = uiState.name.trim(),
        namePlaceholder = "New account",
        saveLabel = if (uiState.isEditing) "Save changes" else "Add account",
        canSave = uiState.canSave,
        onSave = { viewModel.save(onSaved) },
        modifier = modifier,
        manage = uiState.manage,
        itemNoun = "account",
        onArchiveToggle = { viewModel.toggleArchived(onDone) },
        onDelete = { viewModel.delete(onDone) }
    ) {
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::setName,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            label = {
                Text("Name")
            },
            singleLine = true,
            isError = uiState.isDuplicate,
            supportingText = if (uiState.isDuplicate) {
                { Text("An account with this name already exists") }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            // Done closes the keyboard so the type choices are visible.
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        OutlinedTextField(
            value = uiState.openingBalance,
            onValueChange = viewModel::setOpeningBalance,
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Opening balance")
            },
            prefix = {
                Text(currencySymbol())
            },
            placeholder = {
                Text("0")
            },
            supportingText = {
                Text("What the account held before your first transaction here. Use − for money owed.")
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                // Phone layout has a minus key, which number pads often lack.
                keyboardType = KeyboardType.Phone,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            FiscusSectionHeader(title = "Type")

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
            ) {
                AccountType.entries.forEach { type ->
                    val selected = uiState.type == type

                    FilterChip(
                        selected = selected,
                        onClick = { viewModel.setType(type) },
                        label = {
                            Text(accountTypeLabel(type))
                        },
                        leadingIcon = {
                            FiscusChipIcon(
                                selected = selected,
                                icon = accountIcon(type)
                            )
                        }
                    )
                }
            }
        }

        DefaultAccountRow(
            checked = uiState.isDefault,
            enabled = uiState.canChangeDefault,
            onCheckedChange = viewModel::setDefault
        )
    }
}

// Picks the account the add-transaction sheet starts with. The current default stays on
// (locked): turning it on for another account moves it there.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun DefaultAccountRow(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    SegmentedListItem(
        checked = checked,
        onCheckedChange = onCheckedChange,
        enabled = enabled,
        shapes = ListItemDefaults.segmentedShapes(index = 0, count = 1),
        colors = ListItemDefaults.segmentedColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        leadingContent = {
            FiscusListIcon(icon = Icons.Rounded.Star)
        },
        supportingContent = {
            Text(
                text = if (checked && !enabled) {
                    "New transactions start with this account"
                } else {
                    "Start new transactions with this account"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingContent = {
            // The row handles the toggle, so the switch only displays state.
            Switch(
                checked = checked,
                onCheckedChange = null,
                enabled = enabled,
                thumbContent = if (checked) {
                    {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(SwitchDefaults.IconSize)
                        )
                    }
                } else {
                    null
                }
            )
        }
    ) {
        Text(
            text = "Default account",
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
