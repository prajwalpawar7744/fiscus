package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selectableGroup
import androidx.compose.ui.semantics.semantics

// Single-choice connected button group. Options share the width equally, and the
// pressed option grows while its neighbours shrink (Expressive button group motion).
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun <T> FiscusConnectedToggleGroup(
    options: List<T>,
    selectedOption: T,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String,
    modifier: Modifier = Modifier
) {
    val interactionSources = remember(options.size) {
        List(options.size) { MutableInteractionSource() }
    }

    ButtonGroup(
        overflowIndicator = { menuState ->
            ButtonGroupDefaults.OverflowIndicator(menuState = menuState)
        },
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
            customItem(
                buttonGroupContent = {
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
                        interactionSource = interactionSources[index],
                        modifier = Modifier
                            .weight(1f)
                            .animateWidth(interactionSources[index])
                            .semantics {
                                role = Role.RadioButton
                            }
                    ) {
                        Text(
                            text = optionLabel(option),
                            maxLines = 1
                        )
                    }
                },
                // Only shown if the group ever runs out of width.
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = {
                            Text(optionLabel(option))
                        },
                        onClick = {
                            onOptionSelected(option)
                            menuState.dismiss()
                        }
                    )
                }
            )
        }
    }
}
