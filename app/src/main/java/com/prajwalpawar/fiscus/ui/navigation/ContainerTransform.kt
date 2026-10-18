package com.prajwalpawar.fiscus.ui.navigation

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

// Provided around the NavHost and by each destination, so any row or page can opt in
// to the container transform without passing scopes through every screen.
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedVisibilityScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// Keys pairing a tapped row or card with the page it opens.
object ContainerKeys {
    fun transaction(id: Long) = "transaction-$id"
    fun account(id: Long) = "account-$id"
    fun category(id: Long) = "category-$id"
    const val NEW_ACCOUNT = "account-new"
    fun newCategory(type: String) = "category-new-$type"
    const val MANAGE_CATEGORIES = "manage-categories"
    const val MANAGE_ACCOUNTS = "manage-accounts"
    const val PROFILE = "profile"
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
    val motionScheme = MaterialTheme.motionScheme
    val containerColor = MaterialTheme.colorScheme.surface

    return with(sharedScope) {
        this@containerTransform
            .sharedBounds(
                sharedContentState = rememberSharedContentState(key),
                animatedVisibilityScope = visibilityScope,
                enter = fadeIn(motionScheme.defaultEffectsSpec()),
                exit = fadeOut(motionScheme.fastEffectsSpec()),
                boundsTransform = BoundsTransform { _: Rect, _: Rect ->
                    motionScheme.defaultSpatialSpec()
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
