package com.prajwalpawar.fiscus.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Backup
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.window.DialogProperties
import com.prajwalpawar.fiscus.data.backup.BackupPart
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing
import java.text.DateFormat
import java.util.Date

internal fun BackupPart.label(): String = when (this) {
    BackupPart.MONEY -> "Transactions, accounts and categories"
    BackupPart.SETTINGS -> "Settings"
    BackupPart.PROFILE -> "Profile name and photo"
}

// Choose what to back up, or what to restore from a backup ([available] = what it holds).
// Baseline AlertDialog: M3 Expressive has no dialog variant; its buttons get shape morph.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun BackupPartsDialog(
    restoring: Boolean,
    available: Set<BackupPart>,
    createdAt: Long?,
    onConfirm: (Set<BackupPart>) -> Unit,
    onDismiss: () -> Unit
) {
    var selected by rememberSaveable { mutableStateOf(available.map { it.name }.toSet()) }
    val chosen = available.filter { it.name in selected }.toSet()

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (restoring) Icons.Rounded.Restore else Icons.Rounded.Backup,
                contentDescription = null
            )
        },
        title = { Text(if (restoring) "Restore backup" else "Back up") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(FiscusSpacing.xs)) {
                Text(
                    text = if (restoring) {
                        val date = createdAt?.let { DateFormat.getDateTimeInstance().format(Date(it)) }
                        "Made ${date ?: "earlier"}. What you restore replaces what's on this phone now, and Fiscus restarts."
                    } else {
                        "Choose what to save. The file stays wherever you save it."
                    }
                )

                available.forEach { part ->
                    val checked = part.name in selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .toggleable(
                                value = checked,
                                role = Role.Checkbox,
                                onValueChange = { on ->
                                    selected = if (on) selected + part.name else selected - part.name
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // The row handles the toggle, so the checkbox only displays state.
                        Checkbox(checked = checked, onCheckedChange = null)
                        Text(
                            text = part.label(),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(start = FiscusSpacing.sm)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(chosen) },
                enabled = chosen.isNotEmpty(),
                shapes = ButtonDefaults.shapes()
            ) {
                Text(if (restoring) "Restore" else "Choose file")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun WipeDataDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Rounded.DeleteForever, contentDescription = null) },
        title = { Text("Wipe all data?") },
        text = {
            Text(
                "Every transaction, account, category, setting and your profile will be deleted, " +
                    "and Fiscus starts over like a fresh install. Back up first if you might want it back. " +
                    "This can't be undone."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                shapes = ButtonDefaults.shapes(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Wipe data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, shapes = ButtonDefaults.shapes()) {
                Text("Cancel")
            }
        }
    )
}

// Shown while a backup, restore or wipe runs; it can't be dismissed.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DataBusyDialog() {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        icon = { LoadingIndicator() },
        text = { Text("Working on it…") },
        confirmButton = {}
    )
}
