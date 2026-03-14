package com.prajwalpawar.budgetear.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun ConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
    title: String,
    text: String,
    confirmButtonText: String = "Confirm",
    dismissButtonText: String = "Cancel",
    icon: ImageVector? = null,
    confirmButtonColor: ButtonColors = ButtonDefaults.textButtonColors(
        contentColor = MaterialTheme.colorScheme.error
    )
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = confirmButtonColor
            ) {
                Text(confirmButtonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(dismissButtonText)
            }
        },
        title = {
            Text(text = title)
        },
        text = {
            Text(text = text)
        },
        icon = icon?.let {
            { Icon(imageVector = it, contentDescription = null) }
        }
    )
}
