package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalIconToggleButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
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
import com.prajwalpawar.fiscus.data.model.CategoryType
import com.prajwalpawar.fiscus.ui.components.FiscusConnectedToggleGroup
import com.prajwalpawar.fiscus.ui.components.FiscusSectionHeader
import com.prajwalpawar.fiscus.ui.components.categoryIcon
import com.prajwalpawar.fiscus.ui.components.categoryIconOptions
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun NewCategoryScreen(
    viewModel: NewCategoryViewModel,
    onSaved: (id: Long, type: CategoryType) -> Unit,
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
        previewIcon = categoryIcon(uiState.iconKey),
        previewName = uiState.name.trim(),
        namePlaceholder = "New category",
        saveLabel = "Add category",
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
                { Text("A category with this name already exists") }
            } else {
                null
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            // Done closes the keyboard so the type and icon choices are visible.
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            FiscusSectionHeader(title = "Type")

            FiscusConnectedToggleGroup(
                options = CategoryType.entries,
                selectedOption = uiState.type,
                onOptionSelected = viewModel::setType,
                optionLabel = { type ->
                    when (type) {
                        CategoryType.EXPENSE -> "Expense"
                        CategoryType.INCOME -> "Income"
                    }
                }
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            FiscusSectionHeader(title = "Icon")

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
            ) {
                categoryIconOptions.forEach { (key, icon) ->
                    // Toggle shapes morph from round to square when selected.
                    FilledTonalIconToggleButton(
                        checked = uiState.iconKey == key,
                        onCheckedChange = { viewModel.setIcon(key) },
                        shapes = IconButtonDefaults.toggleableShapes(),
                        modifier = Modifier.size(IconButtonDefaults.mediumContainerSize())
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = iconLabel(key)
                        )
                    }
                }
            }
        }
    }
}

// "directions_car" -> "Directions car", for screen readers.
private fun iconLabel(key: String): String {
    return key.replace('_', ' ').replaceFirstChar { it.uppercase() }
}
