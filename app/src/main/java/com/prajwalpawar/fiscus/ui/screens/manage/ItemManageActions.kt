package com.prajwalpawar.fiscus.ui.screens.manage

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

// What an existing category or account allows, decided by its editor ViewModel.
data class ItemManageState(
    val isArchived: Boolean,
    val isDefault: Boolean,
    val usageCount: Int,
    // False when this is the last active item, which must stay usable.
    val canArchive: Boolean
) {
    val canDelete: Boolean get() = !isDefault && usageCount == 0
}

// Archive/restore and delete for an existing item. Delete only appears when nothing
// uses the item; otherwise the hint explains why archiving is the way to hide it.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ItemManageActions(
    state: ItemManageState,
    // "category" or "account", for the messages.
    itemNoun: String,
    onArchiveToggle: () -> Unit,
    onDelete: () -> Unit
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
    ) {
        OutlinedButton(
            onClick = onArchiveToggle,
            enabled = state.isArchived || state.canArchive,
            shapes = ButtonDefaults.shapes(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (state.isArchived) Icons.Filled.Unarchive else Icons.Filled.Archive,
                contentDescription = null
            )
            Spacer(Modifier.width(ButtonDefaults.IconSpacing))
            Text(if (state.isArchived) "Restore" else "Archive")
        }

        if (state.canDelete) {
            TextButton(
                onClick = { confirmDelete = true },
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = null
                )
                Spacer(Modifier.width(ButtonDefaults.IconSpacing))
                Text("Delete")
            }
        }

        hint(state, itemNoun)?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Delete,
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
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun hint(state: ItemManageState, itemNoun: String): String? {
    return when {
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
