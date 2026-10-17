package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

// The full-width, medium Expressive button that finishes a form.
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun FiscusSubmitButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonHeight = ButtonDefaults.MediumContainerHeight

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = buttonHeight),
        enabled = enabled,
        shapes = ButtonDefaults.shapes(),
        contentPadding = ButtonDefaults.contentPaddingFor(
            buttonHeight = buttonHeight,
            hasStartIcon = true
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.iconSizeFor(buttonHeight))
        )

        Spacer(
            modifier = Modifier.width(ButtonDefaults.iconSpacingFor(buttonHeight))
        )

        Text(
            text = text,
            style = ButtonDefaults.textStyleFor(buttonHeight)
        )
    }
}
