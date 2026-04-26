package com.prajwalpawar.fiscus.ui.utils

import androidx.compose.animation.core.*
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
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
import androidx.compose.ui.graphics.Color

/**
 * Reusable animation specs for consistency
 */
object FiscusAnimation {
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
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "staggerProgress"
    )
    val delay = remember(index, initialDelay) { (initialDelay + (index.coerceAtMost(8) * 45)) }

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

    val interactionSource =
        remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
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
 * A modifier that applies a scale-in entry animation.
 */
fun Modifier.fiscusScaleIn(
    enabled: Boolean = true,
    initialScale: Float = 0.8f,
    delay: Int = 0
): Modifier = composed {
    if (!enabled) return@composed this

    var visible by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (visible) 1f else initialScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scaleIn"
    )
    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(300),
        label = "scaleInAlpha"
    )

    LaunchedEffect(Unit) {
        if (delay > 0) kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        this.alpha = alpha
    }
}

/**
 * A modifier that adds a continuous pulsating effect.
 * Useful for highlighting buttons or empty states.
 */
fun Modifier.pulsate(
    enabled: Boolean = true,
    minScale: Float = 0.95f,
    duration: Int = 1200
): Modifier = composed {
    if (!enabled) return@composed this

    val infiniteTransition =
        androidx.compose.animation.core.rememberInfiniteTransition(label = "pulsate")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = minScale,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = tween(duration, easing = LinearOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulsateScale"
    )

    this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}

/**
 * A combined modifier for consistent click behavior with haptics and spring-based scaling.
 */
fun Modifier.fiscusClickable(
    haptic: FiscusHaptic? = null,
    enabledAnimations: Boolean = true,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource =
        remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabledAnimations) 0.96f else 1f,
        animationSpec = FiscusAnimation.SpringLowBouncy,
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
fun rememberFiscusHaptic(): FiscusHaptic {
    val haptic = LocalHapticFeedback.current
    return remember(haptic) { FiscusHaptic(haptic) }
}

class FiscusHaptic(private val haptic: HapticFeedback) {
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
    enabled: Boolean = true,
    isMasked: Boolean = false,
    isCompact: Boolean = false
) {
    if (!enabled || isMasked) {
        Text(
            text = formatCurrency(targetAmount, currencyCode, isMasked, isCompact),
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
        text = formatCurrency(animatedAmount.toDouble(), currencyCode, isCompact = isCompact),
        style = style,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = overflow,
        modifier = modifier
    )
}
