package com.prajwalpawar.fiscus.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.prajwalpawar.fiscus.ui.theme.FiscusMotion
import com.prajwalpawar.fiscus.ui.theme.FiscusReducedMotion

// Lazy list rows that appear, leave and move smoothly instead of popping in (M3: "avoid
// content shifting positions or instantly popping in"). Opacity uses effects springs (no
// overshoot), movement a spatial spring. Reduced motion: a short fade, no movement.
// Needs a stable item key.
@Composable
fun LazyItemScope.fiscusAnimateItem(): Modifier {
    val motionScheme = MaterialTheme.motionScheme
    return if (FiscusReducedMotion.enabled) {
        Modifier.animateItem(
            fadeInSpec = tween(FiscusMotion.REDUCED_FADE_MILLIS),
            placementSpec = null,
            fadeOutSpec = tween(FiscusMotion.REDUCED_FADE_MILLIS)
        )
    } else {
        Modifier.animateItem(
            fadeInSpec = motionScheme.defaultEffectsSpec(),
            placementSpec = motionScheme.defaultSpatialSpec(),
            fadeOutSpec = motionScheme.fastEffectsSpec()
        )
    }
}
