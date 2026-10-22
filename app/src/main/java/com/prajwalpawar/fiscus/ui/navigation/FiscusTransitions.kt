package com.prajwalpawar.fiscus.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.navigation.NavBackStackEntry
import com.prajwalpawar.fiscus.ui.theme.FiscusMotion

// Which M3 transition pattern a navigation uses (m3.material.io → Motion → Transitions).
enum class FiscusTransition {
    // Bottom-bar tabs: top-level fade through, no slide or scale.
    TOP_LEVEL,

    // Hierarchy (Settings → Categories → a category): Android's forward/backward, a short
    // slide with a fade.
    FORWARD_BACKWARD,

    // A list row or card growing into its page. M3 keeps this for hero moments in shallow
    // hierarchies, not for every navigation.
    CONTAINER_TRANSFORM
}

private val tabRoutes = listOf(
    FiscusDestination.Home.route,
    FiscusDestination.Transactions.route,
    FiscusDestination.Analysis.route,
    FiscusDestination.Settings.route
)

private fun NavBackStackEntry.route(): String = destination.route.orEmpty()

private fun isTab(route: String) = tabRoutes.any { route == it }

// The pattern for going from [from] to [to] (and back again).
fun transitionBetween(from: String, to: String): FiscusTransition = when {
    isTab(from) && isTab(to) -> FiscusTransition.TOP_LEVEL
    // A transaction opens from a row: Home, Transactions, Analysis or search.
    to.startsWith(FiscusRoutes.TRANSACTION_DETAILS_BASE) ||
        from.startsWith(FiscusRoutes.TRANSACTION_DETAILS_BASE) && isTab(to) ->
        FiscusTransition.CONTAINER_TRANSFORM
    // An account card on Home grows into its page; the same page from the Accounts list
    // is ordinary hierarchy.
    from == FiscusDestination.Home.route && to.isAccountPage() ||
        to == FiscusDestination.Home.route && from.isAccountPage() ->
        FiscusTransition.CONTAINER_TRANSFORM
    else -> FiscusTransition.FORWARD_BACKWARD
}

private fun String.isAccountPage() =
    startsWith(FiscusRoutes.EDIT_ACCOUNT_BASE) || this == FiscusRoutes.NEW_ACCOUNT

private fun fadeInAfterOut() = fadeIn(
    tween(FiscusMotion.FADE_IN_MILLIS, FiscusMotion.FADE_OUT_MILLIS, FiscusMotion.FadeIn)
)

private fun quickFadeOut() = fadeOut(tween(FiscusMotion.FADE_OUT_MILLIS, easing = FiscusMotion.FadeOut))

private fun reducedIn() = fadeIn(tween(FiscusMotion.REDUCED_FADE_MILLIS))
private fun reducedOut() = fadeOut(tween(FiscusMotion.REDUCED_FADE_MILLIS))

private fun slideSpec() = tween<androidx.compose.ui.unit.IntOffset>(
    FiscusMotion.TRANSITION_MILLIS,
    easing = FiscusMotion.Emphasized
)

private fun offset(fullWidth: Int) = (fullWidth * FiscusMotion.FORWARD_SLIDE_FRACTION).toInt()

// NavHost transitions. [reducedMotion]: every pattern becomes a subtle fade.
class FiscusNavTransitions(private val reducedMotion: Boolean) {
    val enter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (reducedMotion) {
            reducedIn()
        } else when (transitionBetween(initialState.route(), targetState.route())) {
            FiscusTransition.FORWARD_BACKWARD ->
                slideInHorizontally(slideSpec()) { offset(it) } + fadeInAfterOut()
            // Container transform: the shared bounds move; the page itself only fades.
            else -> fadeInAfterOut()
        }
    }

    val exit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (reducedMotion) {
            reducedOut()
        } else when (transitionBetween(initialState.route(), targetState.route())) {
            FiscusTransition.FORWARD_BACKWARD ->
                slideOutHorizontally(slideSpec()) { -offset(it) } + quickFadeOut()
            else -> quickFadeOut()
        }
    }

    val popEnter: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
        if (reducedMotion) {
            reducedIn()
        } else when (transitionBetween(targetState.route(), initialState.route())) {
            FiscusTransition.FORWARD_BACKWARD ->
                slideInHorizontally(slideSpec()) { -offset(it) } + fadeInAfterOut()
            else -> fadeInAfterOut()
        }
    }

    val popExit: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
        if (reducedMotion) {
            reducedOut()
        } else when (transitionBetween(targetState.route(), initialState.route())) {
            FiscusTransition.FORWARD_BACKWARD ->
                slideOutHorizontally(slideSpec()) { offset(it) } + quickFadeOut()
            else -> quickFadeOut()
        }
    }
}
