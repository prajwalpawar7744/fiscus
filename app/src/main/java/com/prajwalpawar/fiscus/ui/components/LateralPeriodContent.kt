package com.prajwalpawar.fiscus.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import com.prajwalpawar.fiscus.ui.theme.FiscusMotion
import com.prajwalpawar.fiscus.ui.theme.FiscusReducedMotion
import com.prajwalpawar.fiscus.util.Period

// M3 lateral transition for peer content (one period → another): the period's content
// slides in as one group from the side you're moving to, without fading, while the period
// switcher above stays put like tabs. Changes within a period (filters, new rows) don't
// slide. Reduced motion: a short crossfade instead.
@Composable
fun <S> LateralPeriodContent(
    state: S,
    period: (S) -> Period,
    modifier: Modifier = Modifier,
    content: @Composable (S) -> Unit
) {
    val reducedMotion = FiscusReducedMotion.enabled
    val slideSpec = tween<IntOffset>(FiscusMotion.TRANSITION_MILLIS, easing = FiscusMotion.Emphasized)

    AnimatedContent(
        targetState = state,
        contentKey = period,
        modifier = modifier,
        transitionSpec = {
            if (reducedMotion) {
                fadeIn(tween(FiscusMotion.REDUCED_FADE_MILLIS)) togetherWith
                    fadeOut(tween(FiscusMotion.REDUCED_FADE_MILLIS))
            } else {
                // Later periods come in from the right, earlier ones from the left.
                val forward = period(targetState).start > period(initialState).start
                val direction = if (forward) 1 else -1
                slideInHorizontally(slideSpec) { width -> width * direction } togetherWith
                    slideOutHorizontally(slideSpec) { width -> -width * direction }
            }
        },
        label = "lateralPeriod"
    ) { shown ->
        content(shown)
    }
}
