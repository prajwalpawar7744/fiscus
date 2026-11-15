package com.prajwalpawar.fiscus.ui.components

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.prajwalpawar.fiscus.R
import com.prajwalpawar.fiscus.ui.theme.FiscusLayout
import com.prajwalpawar.fiscus.ui.theme.FiscusTheme

/** How an action in an [ActionButtonGroup] looks. */
enum class ActionStyle {
    /** The main action: filled `Button` in the primary roles. */
    Primary,

    /** A secondary action: `OutlinedButton`. */
    Secondary,

    /** A destructive action: filled `Button` in the error-container roles (ADR-027). */
    Danger,
}

/** One action of an [ActionButtonGroup]. While [isLoading], its content is a [LoadingIndicator]. */
data class GroupAction(
    val label: String,
    val onClick: () -> Unit,
    @DrawableRes val icon: Int? = null,
    val style: ActionStyle = ActionStyle.Primary,
    val enabled: Boolean = true,
    val isLoading: Boolean = false,
)

/**
 * Shared component: **every page's actions** (editor Save, detail Delete/Edit, filter sheet
 * Clear/Done) in one M3 Expressive [ButtonGroup] (ADR-028). Medium-height buttons share the width
 * evenly; the pressed one widens and its neighbours make room (built-in motion). Overflow items go
 * to the group's menu. A loading action cross-fades to a [LoadingIndicator] on the theme's
 * effects spring (no scale, M3 enter/exit patterns).
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class) // button size helpers, LoadingIndicator
@Composable
fun ActionButtonGroup(actions: List<GroupAction>, modifier: Modifier = Modifier) {
    val height = ButtonDefaults.MediumContainerHeight
    ButtonGroup(
        overflowIndicator = { menuState -> ButtonGroupDefaults.OverflowIndicator(menuState) },
        modifier = modifier.fillMaxWidth(),
    ) {
        actions.forEach { action ->
            customItem(
                buttonGroupContent = {
                    val interactionSource = remember { MutableInteractionSource() }
                    val buttonModifier = Modifier
                        .weight(1f)
                        .heightIn(min = height)
                        .animateWidth(interactionSource)
                    val enabled = action.enabled && !action.isLoading
                    when (action.style) {
                        ActionStyle.Secondary -> OutlinedButton(
                            onClick = action.onClick,
                            enabled = enabled,
                            shapes = ButtonDefaults.shapesFor(height),
                            contentPadding = ButtonDefaults.contentPaddingFor(height),
                            interactionSource = interactionSource,
                            modifier = buttonModifier,
                        ) { ActionContent(action) }
                        else -> Button(
                            onClick = action.onClick,
                            enabled = enabled,
                            shapes = ButtonDefaults.shapesFor(height),
                            colors = action.style.buttonColors(),
                            contentPadding = ButtonDefaults.contentPaddingFor(height),
                            interactionSource = interactionSource,
                            modifier = buttonModifier,
                        ) { ActionContent(action) }
                    }
                },
                menuContent = { menuState ->
                    DropdownMenuItem(
                        text = { Text(action.label) },
                        enabled = action.enabled && !action.isLoading,
                        onClick = {
                            menuState.dismiss()
                            action.onClick()
                        },
                    )
                },
            )
        }
    }
}

@Composable
private fun ActionStyle.buttonColors(): ButtonColors = when (this) {
    ActionStyle.Danger -> ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
    )
    else -> ButtonDefaults.buttonColors()
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class) // button size helpers, LoadingIndicator
@Composable
private fun ActionContent(action: GroupAction) {
    val height = ButtonDefaults.MediumContainerHeight
    val motion = MaterialTheme.motionScheme
    AnimatedContent(
        targetState = action.isLoading,
        transitionSpec = {
            fadeIn(motion.fastEffectsSpec())
                .togetherWith(fadeOut(motion.fastEffectsSpec()))
                .using(SizeTransform { _, _ -> motion.fastSpatialSpec() })
        },
        contentAlignment = Alignment.Center,
        label = "actionContent",
    ) { loading ->
        if (loading) {
            LoadingIndicator(modifier = Modifier.size(ButtonDefaults.iconSizeFor(height)))
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (action.icon != null) {
                    Icon(
                        painter = painterResource(action.icon),
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.iconSizeFor(height)),
                    )
                    Spacer(Modifier.size(ButtonDefaults.iconSpacingFor(height)))
                }
                Text(action.label, style = ButtonDefaults.textStyleFor(height), maxLines = 1)
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Large font", showBackground = true, fontScale = 2f)
@Composable
private fun ActionButtonGroupPreview() {
    FiscusTheme(dynamicColor = false) {
        Surface {
            Column(modifier = Modifier.padding(FiscusLayout.ScreenMargin)) {
                ActionButtonGroup(
                    actions = listOf(
                        GroupAction("Delete", {}, R.drawable.ic_delete_rounded, ActionStyle.Danger),
                        GroupAction("Edit", {}, R.drawable.ic_edit_rounded),
                    ),
                )
                Spacer(Modifier.size(FiscusLayout.ItemSpacing))
                ActionButtonGroup(
                    actions = listOf(
                        GroupAction("Clear", {}, style = ActionStyle.Secondary),
                        GroupAction("Done", {}),
                    ),
                )
                Spacer(Modifier.size(FiscusLayout.ItemSpacing))
                ActionButtonGroup(actions = listOf(GroupAction("Save", {}, isLoading = true)))
            }
        }
    }
}
