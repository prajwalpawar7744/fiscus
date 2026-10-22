package com.prajwalpawar.fiscus.ui.theme

import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState

// True when the user turned on "Remove animations" (Accessibility), which sets the system
// animator scale to 0. M3: then use subtle fades instead of sliding or scaling, and drop
// decorative effects like shape morphing.
val LocalReducedMotion = compositionLocalOf { false }

object FiscusReducedMotion {
    val enabled: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalReducedMotion.current
}

// Re-read whenever the app comes back to the foreground, since the setting lives in the
// system Settings app.
@Composable
fun rememberSystemReducedMotion(): Boolean {
    val context = LocalContext.current
    val lifecycleState = LocalLifecycleOwner.current.lifecycle.currentStateAsState()
    val resumed = lifecycleState.value.isAtLeast(Lifecycle.State.RESUMED)
    return remember(context, resumed) {
        Settings.Global.getFloat(
            context.contentResolver,
            Settings.Global.ANIMATOR_DURATION_SCALE,
            1f
        ) == 0f
    }
}
