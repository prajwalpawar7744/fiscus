package com.prajwalpawar.fiscus.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing

// Shared motion values. Component motion comes from MaterialTheme.motionScheme (springs);
// screen transitions follow M3's transition patterns, which still use the easing-and-
// duration system (m3.material.io → Motion → Transitions), so their timings live here.
object FiscusMotion {
    // Starting scale for small content that pops in place (an icon changing).
    const val ENTER_SCALE: Float = 0.92f

    // Home balance hero press: the card gives a little and its decorative shape turns.
    const val HERO_PRESS_SCALE: Float = 0.97f
    const val HERO_PRESS_ROTATION: Float = 30f

    // One full turn of the hero's decorative shape: slow enough to stay in the background.
    const val HERO_SPIN_MILLIS: Int = 24_000

    // Transition patterns: 300 ms in all; the old screen fades out in the first 90 ms and
    // the new one fades in over the remaining 210 ms ("fade out fully before fading in").
    const val TRANSITION_MILLIS: Int = 300
    const val FADE_OUT_MILLIS: Int = 90
    const val FADE_IN_MILLIS: Int = TRANSITION_MILLIS - FADE_OUT_MILLIS

    // Forward/backward: Android slides a short way while fading (not the full width).
    const val FORWARD_SLIDE_FRACTION: Float = 0.1f

    // With "Remove animations" on: a short, subtle fade only.
    const val REDUCED_FADE_MILLIS: Int = 150

    // M3 emphasized easing for screen transitions; linear-out-slow-in / fast-out-linear-in
    // for the fades.
    val Emphasized: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
    val FadeIn: Easing = CubicBezierEasing(0f, 0f, 0.2f, 1f)
    val FadeOut: Easing = CubicBezierEasing(0.4f, 0f, 1f, 1f)
}
