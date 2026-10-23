package com.prajwalpawar.fiscus.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SplitButtonDefaults
import androidx.compose.material3.SplitButtonLayout
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import com.prajwalpawar.fiscus.ui.theme.FiscusReducedMotion
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

// A related alternative to a split button's main action.
data class FiscusMenuAction(
    val label: String,
    val icon: ImageVector,
    val onClick: () -> Unit
)

// M3 split button: a main action plus ▾ for a menu of related actions. The ▾ rotates 180°
// with the standard (not expressive) motion when the menu opens, its menu is aligned under
// it, and it carries an expanded/collapsed state and a tooltip for TalkBack (as in the
// official samples). [fillWidth] stretches the main button, like FiscusSubmitButton.
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FiscusSplitButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    menuDescription: String,
    actions: List<FiscusMenuAction>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fillWidth: Boolean = false,
    height: Dp = SplitButtonDefaults.SmallContainerHeight,
    colors: ButtonColors = ButtonDefaults.buttonColors()
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = if (FiscusReducedMotion.enabled) snap() else MaterialTheme.motionScheme.defaultSpatialSpec(),
        label = "splitArrow"
    )

    Box(modifier = modifier) {
        SplitButtonLayout(
            modifier = if (fillWidth) Modifier.fillMaxWidth() else Modifier,
            leadingButton = {
                SplitButtonDefaults.LeadingButton(
                    onClick = onClick,
                    enabled = enabled,
                    colors = colors,
                    modifier = Modifier
                        .heightIn(min = height)
                        .then(if (fillWidth) Modifier.fillMaxWidth() else Modifier),
                    shapes = SplitButtonDefaults.leadingButtonShapesFor(height),
                    contentPadding = SplitButtonDefaults.leadingButtonContentPaddingFor(height)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(SplitButtonDefaults.leadingButtonIconSizeFor(height))
                    )
                    Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(height)))
                    Text(text = text, style = ButtonDefaults.textStyleFor(height))
                }
            },
            trailingButton = {
                // The menu is anchored to the ▾ button, so it lines up with its edge.
                Box {
                    TooltipBox(
                        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
                        tooltip = { PlainTooltip { Text(menuDescription) } },
                        state = rememberTooltipState()
                    ) {
                        SplitButtonDefaults.TrailingButton(
                            checked = expanded,
                            onCheckedChange = { expanded = it },
                            enabled = enabled,
                            colors = colors,
                            modifier = Modifier
                                .heightIn(min = height)
                                .semantics {
                                    contentDescription = menuDescription
                                    stateDescription = if (expanded) "Expanded" else "Collapsed"
                                },
                            shapes = SplitButtonDefaults.trailingButtonShapesFor(height),
                            contentPadding = SplitButtonDefaults.trailingButtonContentPaddingFor(height)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(SplitButtonDefaults.trailingButtonIconSizeFor(height))
                                    .graphicsLayer { rotationZ = rotation }
                            )
                        }
                    }

                    // M3: the menu sits 4dp below, aligned with the trailing button.
                    DropdownMenuPopup(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        popupPositionProvider = MenuDefaults.rememberDropdownMenuPopupPositionProvider(
                            dropdownMenuAnchorPosition = MenuAnchorPosition.Below,
                            offset = DpOffset(FiscusSpacing.none, FiscusSpacing.xs)
                        )
                    ) {
                        DropdownMenuGroup(shapes = MenuDefaults.groupShape(index = 0, count = 1)) {
                            actions.forEachIndexed { index, action ->
                                DropdownMenuItem(
                                    onClick = {
                                        expanded = false
                                        action.onClick()
                                    },
                                    text = { Text(action.label) },
                                    shape = MenuDefaults.itemShape(index = index, count = actions.size).shape,
                                    leadingIcon = { Icon(action.icon, contentDescription = null) }
                                )
                            }
                        }
                    }
                }
            }
        )
    }
}
