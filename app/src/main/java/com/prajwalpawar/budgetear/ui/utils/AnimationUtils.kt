package com.prajwalpawar.budgetear.ui.utils

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.graphics.Color

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
        val Enter: EnterTransition = fadeIn(animationSpec = tween(250))
        val Exit: ExitTransition = fadeOut(animationSpec = tween(250))
        val PopEnter: EnterTransition = fadeIn(animationSpec = tween(250))
        val PopExit: ExitTransition = fadeOut(animationSpec = tween(250))
    }
}

/**
 * A modifier that applies a staggered vertical slide and fade-in animation.
 * Ideal for list items and section cards.
 */
fun Modifier.staggeredVerticalFadeIn(
    index: Int,
    enabled: Boolean = true,
    initialDelay: Long = 100
): Modifier = composed {
    if (!enabled) return@composed this

    // A single 0→1 progress drives both alpha and translationY via one graphicsLayer call.
    // No recomposition, no layout pass — purely GPU-side alpha blending + translation.
    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 300,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "staggerProgress"
    )
    val delay = remember(index, initialDelay) { (initialDelay + (index.coerceAtMost(8) * 45)).toLong() }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay)
        progress = 1f
    }

    this.graphicsLayer {
        alpha = animatedProgress
        translationY = (1f - animatedProgress) * 24f  // subtle 24px upward slide
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
        animationSpec = BudgetearAnimation.SpringLowBouncy,
        label = "clickScale"
    )

    this.graphicsLayer {
            scaleX = scale
            scaleY = scale
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

/**
 * A Text component that animates its numerical value from 0 when it first appears.
 */
@Composable
fun AnimatedAmount(
    targetAmount: Double,
    currencyCode: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleMedium,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = MaterialTheme.colorScheme.onSurface,
    textAlign: TextAlign = TextAlign.Start,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Clip,
    enabled: Boolean = true
) {
    if (!enabled) {
        Text(
            text = formatCurrency(targetAmount, currencyCode),
            style = style,
            fontWeight = fontWeight,
            color = color,
            textAlign = textAlign,
            maxLines = maxLines,
            overflow = overflow,
            modifier = modifier
        )
        return
    }

    var animateTrigger by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(Unit) {
        animateTrigger = 1f
    }

    val animatedAmount by animateFloatAsState(
        targetValue = if (animateTrigger == 1f) targetAmount.toFloat() else 0f,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "amountAnimation"
    )

    Text(
        text = formatCurrency(animatedAmount.toDouble(), currencyCode),
        style = style,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier
    )
}
