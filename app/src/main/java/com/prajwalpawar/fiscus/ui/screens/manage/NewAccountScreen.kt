package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.data.model.AccountType
import com.prajwalpawar.fiscus.ui.components.FiscusChipIcon
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.accountIcon
import com.prajwalpawar.fiscus.ui.components.accountTypeLabel
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewAccountScreen(
    viewModel: NewAccountViewModel,
    onSaved: (id: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Typing a name is the one required step, so start there.
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    NewItemLayout(
        previewIcon = accountIcon(uiState.type),
        previewName = uiState.name.trim(),
        namePlaceholder = "New account",
        previewShape = MaterialShapes.Clover4Leaf,
        saveLabel = "Add account",
        canSave = uiState.canSave,
        onSave = { viewModel.save(onSaved) },
        modifier = modifier
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
    }
}
