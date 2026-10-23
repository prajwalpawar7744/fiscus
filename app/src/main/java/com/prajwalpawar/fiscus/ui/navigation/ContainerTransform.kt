package com.prajwalpawar.fiscus.ui.navigation

import com.prajwalpawar.fiscus.ui.theme.FiscusMotion
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SharedTransitionScope.ResizeMode.Companion.RemeasureToBounds
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Shape
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.prajwalpawar.fiscus.ui.theme.FiscusReducedMotion

// Provided around the NavHost and by each destination, so any row or page can opt in
// to the container transform without passing scopes through every screen.
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// Keys pairing a tapped row or card with the page it opens. M3 keeps the container
// transform for hero moments: a transaction row opening its details, and an account card
// on Home growing into its page. Other pages use forward/backward (FiscusTransitions).
object ContainerKeys {
    fun transaction(id: Long) = "transaction-$id"
    fun account(id: Long) = "account-$id"
    const val NEW_ACCOUNT = "account-new"
}

// Material container transform: the element tagged with [key] on the current page grows
// into the element with the same key on the next page (and shrinks back on return).
// Put it on the tapped row/card and on the destination page's root. Does nothing outside
// the NavHost (e.g. inside the add-transaction sheet, which is a separate window).
@Composable
fun Modifier.containerTransform(
    key: Any,
    clipShape: Shape = MaterialTheme.shapes.large
): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavAnimatedVisibilityScope.current ?: return this
    // With "Remove animations" on, pages just fade (M3 reduced motion).
    if (FiscusReducedMotion.enabled) return this
    val motionScheme = MaterialTheme.motionScheme
    val containerColor = MaterialTheme.colorScheme.surface

    return with(sharedScope) {
        this@containerTransform
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = visibilityScope,
                enter = fadeIn(motionScheme.defaultEffectsSpec()),
                exit = fadeOut(motionScheme.fastEffectsSpec()),
                // M3 transitions use easing + duration, never bouncy springs: an expressive
                // spring overshot when a predictive back swipe was released (the card flew
                // past its spot in the carousel, then sprang back). Same timing as the other
                // screen transitions, and it follows the back gesture smoothly.
                boundsTransform = BoundsTransform { _: Rect, _: Rect ->
                    tween(FiscusMotion.TRANSITION_MILLIS, easing = FiscusMotion.Emphasized)
                },
                // Relayout at each size instead of scaling, so the page reflows as it grows.
                resizeMode = RemeasureToBounds,
                clipInOverlayDuringTransition = OverlayClip(clipShape)
            )
            // An opaque container, so the growing page doesn't show the screen behind it.
            .background(containerColor)
    }
}

// A NavHost destination that exposes its animation scope to containerTransform.
fun NavGraphBuilder.fiscusComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable (NavBackStackEntry) -> Unit
) {
    composable(route = route, arguments = arguments) { entry ->
        CompositionLocalProvider(LocalNavAnimatedVisibilityScope provides this) {
            content(entry)
        }
    }
}
