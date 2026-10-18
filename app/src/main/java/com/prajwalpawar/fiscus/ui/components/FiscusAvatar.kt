package com.prajwalpawar.fiscus.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import com.prajwalpawar.fiscus.data.local.ProfilePhotoStore
import com.prajwalpawar.fiscus.ui.theme.FiscusShapes

// The user's profile picture in the chosen icon shape; falls back to their initial,
// or a person icon when there's no name either. Decorative: the name is shown beside it.
@Composable
fun FiscusAvatar(
    name: String,
    photoVersion: Long,
    size: Dp,
    initialStyle: TextStyle,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val store = remember(context) { ProfilePhotoStore(context.applicationContext) }
    val photo by produceState<ImageBitmap?>(initialValue = null, photoVersion) {
        value = if (photoVersion == 0L) null else store.load()?.asImageBitmap()
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(FiscusShapes.icon)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Crossfade(targetState = photo, label = "avatar") { bitmap ->
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                val initial = name.trim().firstOrNull()?.uppercase()
                when {
                    bitmap != null -> Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    initial != null -> Text(
                        text = initial,
                        style = initialStyle,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    else -> Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
