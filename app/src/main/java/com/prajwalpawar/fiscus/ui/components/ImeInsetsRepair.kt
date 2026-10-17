package com.prajwalpawar.fiscus.ui.components

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imeAnimationTarget
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

// Longer than a normal keyboard animation, so a running one is never interrupted.
private const val STALE_IME_RETRY_MS = 300L

// Workaround: Compose's insets listener only applies the final insets when a keyboard
// animation has a fixed duration. Closing the keyboard with the predictive back swipe is a
// user-driven animation without one, so the keyboard's padding stays behind as an empty band.
// The target insets are still updated, so when the padding says "open" but the target says
// "closed", ask the view to dispatch its insets again (harmless while an animation runs).
// Call once per window: the app's window and each dialog/sheet window track insets separately.
// shortcut: remove once foundation-layout applies onEnd for controlled IME animations.
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ImeInsetsRepair() {
    val view = LocalView.current
    val density = LocalDensity.current
    val ime = WindowInsets.ime
    val imeTarget = WindowInsets.imeAnimationTarget

    LaunchedEffect(view) {
        snapshotFlow {
            ime.getBottom(density) > 0 && imeTarget.getBottom(density) == 0
        }.collectLatest { isStale ->
            // Cancelled as soon as the padding catches up with the target.
            while (isStale) {
                delay(STALE_IME_RETRY_MS)
                view.requestApplyInsets()
            }
        }
    }
}
