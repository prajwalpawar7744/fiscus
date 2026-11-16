package com.prajwalpawar.fiscus.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.LocalNavAnimatedContentScope

/** The app's [SharedTransitionScope] (`SharedTransitionLayout` around `NavDisplay`, ADR-028). */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/** The current destination's enter/exit scope; null outside `NavDisplay` (previews, tests). */
val LocalNavVisibilityScope = staticCompositionLocalOf<AnimatedVisibilityScope?> { null }

/**
 * Entry decorator that hands each destination its `NavDisplay` animation scope through
 * [LocalNavVisibilityScope], so [sharedContainer] works inside the app and is a no-op elsewhere
 * (Navigation 3's own local throws outside `NavDisplay`).
 */
fun navVisibilityScopeDecorator(): NavEntryDecorator<NavKey> =
    NavEntryDecorator { entry ->
        val visibilityScope = LocalNavAnimatedContentScope.current
        CompositionLocalProvider(LocalNavVisibilityScope provides visibilityScope) {
            entry.Content()
        }
    }

/**
 * Joins this element to the element with the same [key] on another destination: on navigation
 * it **grows into** the other one, and on Back it **shrinks back** (the M3 container transform).
 * Motion comes from the theme's M3 Expressive tokens (ADR-030): the bounds move on the **slow
 * spatial** spring (M3: slow for full-screen changes), and the contents cross-fade on the effects
 * springs. No app-written values. A no-op outside the app's navigation.
 */
@Composable
fun Modifier.sharedContainer(key: Any): Modifier {
    val sharedScope = LocalSharedTransitionScope.current ?: return this
    val visibilityScope = LocalNavVisibilityScope.current ?: return this
    val motion = MaterialTheme.motionScheme
    val boundsSpec = motion.slowSpatialSpec<Rect>()
    return with(sharedScope) {
        this@sharedContainer.sharedBounds(
            sharedContentState = rememberSharedContentState(key),
            animatedVisibilityScope = visibilityScope,
            enter = fadeIn(motion.defaultEffectsSpec()),
            exit = fadeOut(motion.fastEffectsSpec()),
            boundsTransform = BoundsTransform { _, _ -> boundsSpec },
        )
    }
}

/** The shared-container key that links a transaction's list item and its detail page. */
fun transactionContainerKey(transactionId: Long): String = "transaction-$transactionId"

/** The shared-container key that links an account's card or row and its page (ADR-036). */
fun accountContainerKey(accountId: Long): String = "account-$accountId"
