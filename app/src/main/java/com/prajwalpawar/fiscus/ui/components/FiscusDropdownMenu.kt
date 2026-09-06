package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CheckableDropdownMenuItem
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorPosition
import androidx.compose.material3.MenuDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> FiscusDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    itemLabel: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    offset: DpOffset = DpOffset.Zero,
    content: (@Composable ColumnScope.() -> Unit)? = null
) {
    DropdownMenuPopup(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        popupPositionProvider =
            MenuDefaults.rememberDropdownMenuPopupPositionProvider(
                dropdownMenuAnchorPosition = MenuAnchorPosition.Below,
                offset = offset
            )
    ) {
        DropdownMenuGroup(
            shapes = MenuDefaults.groupShape(
                index = 0,
                count = 1
            )
        ) {
            val itemCount = items.size

            items.forEachIndexed { index, item ->
                CheckableDropdownMenuItem(
                    text = {
                        itemLabel(item)
                    },
                    shapes = MenuDefaults.itemShape(
                        index = index,
                        count = itemCount
                    ),
                    colors = MenuDefaults.selectableItemColors(),
                    checked = item == selectedItem,
                    checkedLeadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                    },
                    onCheckedChange = { checked ->
                        if (checked) {
                            onItemSelected(item)
                        }
                    }
                )
            }

            content?.invoke(this)
        }
    }
}