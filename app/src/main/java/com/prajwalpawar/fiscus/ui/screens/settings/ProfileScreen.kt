package com.prajwalpawar.fiscus.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.prajwalpawar.fiscus.ui.components.FiscusAvatar
import com.prajwalpawar.fiscus.ui.components.FiscusSubmitButton
import com.prajwalpawar.fiscus.ui.theme.FiscusSize
import com.prajwalpawar.fiscus.ui.theme.FiscusSpacing

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ProfileScreen(
    viewModel: SettingsViewModel,
    onSaved: () -> Unit,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    // null until the user types, so the field shows the stored name once it has loaded.
    var editedName by rememberSaveable { mutableStateOf<String?>(null) }
    val name = editedName ?: settings.profileName

    // System Photo Picker: no storage permission, the user picks exactly one image.
    val pickPhoto = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) viewModel.setProfilePhoto(uri)
    }

    val save = {
        focusManager.clearFocus()
        viewModel.setProfileName(name)
        onSaved()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(FiscusSpacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(FiscusSpacing.lg)
    ) {
        FiscusAvatar(
            name = name,
            photoVersion = settings.profilePhotoVersion,
            size = FiscusSize.avatarHero,
            initialStyle = MaterialTheme.typography.displayLargeEmphasized
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(FiscusSpacing.sm)
        ) {
            FilledTonalButton(
                onClick = {
                    pickPhoto.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                shapes = ButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Text(
                    text = if (settings.profilePhotoVersion == 0L) "Choose photo" else "Change photo",
                    modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                )
            }

            if (settings.profilePhotoVersion != 0L) {
                OutlinedButton(
                    onClick = viewModel::removeProfilePhoto,
                    shapes = ButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Text(
                        text = "Remove",
                        modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                    )
                }
            }
        }

        OutlinedTextField(
            value = name,
            onValueChange = { editedName = it.take(MAX_NAME_LENGTH) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Name") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = { save() })
        )

        FiscusSubmitButton(
            text = "Save",
            icon = Icons.Default.Check,
            // Always enabled: the photo is saved on pick, so Save may just confirm and close.
            enabled = true,
            onClick = save
        )
    }
}

private const val MAX_NAME_LENGTH = 40
