package com.prajwalpawar.budgetear.ui.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * Reusable animation specs for consistency
 */
object BudgetearAnimation {
    val SpringLowBouncy: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val SpringMediumBouncy: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    val DefaultTween: AnimationSpec<Float> = tween(
        durationMillis = 400,
        easing = FastOutSlowInEasing
    )
    
    val QuickTween: AnimationSpec<Float> = tween(
        durationMillis = 200,
        easing = LinearOutSlowInEasing
    )

    /**
     * Standard navigation transitions for a smooth, high-quality feel.
     */
    object Navigation {
        val Enter: EnterTransition = slideInHorizontally(
            initialOffsetX = { it / 3 },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(animationSpec = tween(400)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )

        val Exit: ExitTransition = slideOutHorizontally(
            targetOffsetX = { -it / 3 },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(animationSpec = tween(400)) + scaleOut(
            targetScale = 0.95f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )

        val PopEnter: EnterTransition = slideInHorizontally(
            initialOffsetX = { -it / 3 },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeIn(animationSpec = tween(400)) + scaleIn(
            initialScale = 0.95f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )

        val PopExit: ExitTransition = slideOutHorizontally(
            targetOffsetX = { it / 3 },
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        ) + fadeOut(animationSpec = tween(400)) + scaleOut(
            targetScale = 0.95f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
        )
    }
}

/**
 * A modifier that applies a staggered vertical slide and fade-in animation.
 * Ideal for list items and section cards.
 */
fun Modifier.staggeredVerticalFadeIn(
    index: Int,
    enabled: Boolean = true,
    baseDelay: Int = 100,
    staggerDelay: Int = 50
): Modifier = composed {
    if (!enabled) return@composed this

    val visibleState = remember {
        MutableTransitionState(false)
    }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(baseDelay + (index * staggerDelay).toLong())
        visibleState.targetState = true
    }
    
    val transition = updateTransition(visibleState, label = "staggeredFadeIn")
    
    val alpha by transition.animateFloat(
        transitionSpec = { tween(durationMillis = 400) },
        label = "alpha"
    ) { if (it) 1f else 0f }
    
    val offsetY by transition.animateFloat(
        transitionSpec = { 
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = 0.1f
            )
        },
        label = "offsetY"
    ) { if (it) 0f else 40f }

    val scale by transition.animateFloat(
        transitionSpec = {
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        },
        label = "scale"
    ) { if (it) 1f else 0.92f }

    this.graphicsLayer {
        this.alpha = alpha
        this.translationY = offsetY
        this.scaleX = scale
        this.scaleY = scale
    }
}

/**
 * A modifier that adds a subtle spring-based scale animation on click or state change.
 */
fun Modifier.scaleOnPress(enabled: Boolean = true): Modifier = composed {
    if (!enabled) return@composed this
    
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "scaleOnPress"
    )

    this.graphicsLayer {
        this.scaleX = scale
        this.scaleY = scale
    }
}

/**
 * A combined modifier for consistent click behavior with haptics and spring-based scaling.
 */
fun Modifier.budgetearClickable(
    haptic: BudgetearHaptic? = null,
    enabledAnimations: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isPressed && enabledAnimations) 0.96f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "budgetearClickableScale"
    )

    this.graphicsLayer {
            this.scaleX = scale
            this.scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(),
            onClick = {
                haptic?.click()
                onClick()
            }
        )
}

/**
 * Extension for providing standard haptic feedback patterns
 */
@Composable
fun rememberBudgetearHaptic(): BudgetearHaptic {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { BudgetearHaptic(haptic) }
}

class BudgetearHaptic(private val haptic: HapticFeedback) {
    fun click() {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
    
    fun longClick() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }
    
    fun success() {
        // Double tap for success
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    }
}
