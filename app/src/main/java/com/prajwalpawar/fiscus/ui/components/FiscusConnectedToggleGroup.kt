package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics

// Single-choice connected button group; each option takes an equal share of the width.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> FiscusConnectedToggleGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                selectableGroup()
            },
        horizontalArrangement = Arrangement.spacedBy(
            ButtonGroupDefaults.ConnectedSpaceBetween
        )
    ) {
        options.forEachIndexed { index, option ->
            ToggleButton(
                checked = option == selectedOption,
                onCheckedChange = {
                    onOptionSelected(option)
                },
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()

                    options.lastIndex ->
                        ButtonGroupDefaults.connectedTrailingButtonShapes()

                    else ->
                        ButtonGroupDefaults.connectedMiddleButtonShapes()
                },
                modifier = Modifier
                    .weight(1f)
                    .semantics {
                        role = Role.RadioButton
                    }
            ) {
                Text(text = optionLabel(option))
            }
        }
    }
}
