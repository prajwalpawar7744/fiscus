@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.prajwalpawar.fiscus.ui.screens.manage

import com.prajwalpawar.fiscus.ui.components.FiscusTooltip
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material.icons.rounded.Check
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

// What an existing category or account allows, decided by its editor ViewModel.
data class ItemManageState(
    val isArchived: Boolean,
    val isDefault: Boolean,
    val usageCount: Int,
    // False when this is the last active item, which must stay usable.
    val canArchive: Boolean,
    // Why archiving is blocked when it isn't the last-item rule (e.g. the default account).
    val archiveBlockedHint: String? = null
) {
    val canDelete: Boolean get() = !isDefault && usageCount == 0
}

// The editor's page actions, matching the transaction details page: a vibrant floating
// toolbar with Save as its FAB (the main action) and Archive/Restore and Delete inside.
// Unavailable actions stay visible but disabled ([ItemManageHint] says why); Delete asks
// first. Sits above the keyboard so Save stays reachable while typing.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ItemManageToolbar(
    state: ItemManageState,
    // "category" or "account", for the messages.
    itemNoun: String,
    onSave: () -> Unit,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    HorizontalFloatingToolbar(
        expanded = true,
        floatingActionButton = {
            FiscusTooltip("Save changes") {
                FloatingToolbarDefaults.VibrantFloatingActionButton(onClick = onSave) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = "Save changes"
                    )
                }
            }
        },
        colors = FloatingToolbarDefaults.vibrantFloatingToolbarColors(),
        modifier = modifier
    ) {
        val archiveLabel = if (state.isArchived) "Restore" else "Archive"
        FiscusTooltip(archiveLabel) {
            IconButton(
                onClick = onArchiveToggle,
                enabled = state.isArchived || state.canArchive,
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = if (state.isArchived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                    contentDescription = archiveLabel
                )
            }
        }

        FiscusTooltip("Delete") {
            IconButton(
                onClick = { confirmDelete = true },
                enabled = state.canDelete,
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Delete"
                )
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = null
                )
            },
            title = {
                Text("Delete this $itemNoun?")
            },
            text = {
                Text("It isn't used by any transactions. This can't be undone.")
            },
            confirmButton = {
                TextButton(
                    shapes = ButtonDefaults.shapes(),
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }, shapes = ButtonDefaults.shapes()) {
                    Text("Cancel")
                }
            }
        )
    }
}

// Why Archive or Delete is unavailable (or what archiving means), under the form.
@Composable
fun ItemManageHint(
    state: ItemManageState,
    itemNoun: String,
    modifier: Modifier = Modifier
) {
    hint(state, itemNoun)?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = modifier.fillMaxWidth()
        )
    }
}

private fun hint(state: ItemManageState, itemNoun: String): String? {
    return when {
        !state.isArchived && !state.canArchive && state.archiveBlockedHint != null ->
            state.archiveBlockedHint

        !state.isArchived && !state.canArchive ->
            "This is your only active $itemNoun, so it can't be archived."

        state.isDefault && !state.isArchived ->
            "Default ${itemNoun}s can't be deleted, but you can archive them."

        state.usageCount > 0 && !state.isArchived ->
            "Used by ${transactionCount(state.usageCount)}. Archive it to hide it from pickers " +
                    "while keeping its history."

        state.isArchived ->
            "Archived ${itemNoun}s are hidden from pickers. Restore to use it again."

        else -> null
    }
}

fun transactionCount(count: Int): String {
    return if (count == 1) "1 transaction" else "$count transactions"
}
