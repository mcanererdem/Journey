package com.mcanererdem.journey.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Journey Dark Fantasy — Centralized Animation Handlers.
 */
object RpgAnimations {
    
    /**
     * Standard shake effect for damage or impact.
     * @param trigger Change this value to trigger the animation.
     * @param enabled Global toggle to disable animations.
     */
    fun Modifier.shake(trigger: Int, enabled: Boolean = true): Modifier = composed {
        if (!enabled) return@composed this
        
        val shakeOffset = remember { Animatable(0f) }
        LaunchedEffect(trigger) {
            if (trigger > 0) {
                shakeOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = keyframes {
                        durationMillis = 400
                        (-10f) at 50
                        10f at 100
                        (-10f) at 150
                        10f at 200
                        (-5f) at 250
                        5f at 300
                        (-2f) at 350
                        0f at 400
                    }
                )
            }
        }
        this.graphicsLayer(translationX = shakeOffset.value)
    }

    /**
     * Subtle pulse effect for buttons or interactive items.
     */
    @Composable
    fun rememberPulseScale(enabled: Boolean = true): State<Float> {
        if (!enabled) return remember { mutableStateOf(1f) }
        
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        return infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulse_scale"
        )
    }

    /**
     * Floating effect for items or icons.
     */
    @Composable
    fun rememberFloatOffset(enabled: Boolean = true): State<Float> {
        if (!enabled) return remember { mutableStateOf(0f) }
        
        val infiniteTransition = rememberInfiniteTransition(label = "float")
        return infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = -8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOut),
                repeatMode = RepeatMode.Reverse
            ),
            label = "float_offset"
        )
    }
}
