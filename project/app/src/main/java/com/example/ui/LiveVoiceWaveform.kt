package com.example.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import kotlin.math.sin

@Composable
fun LiveVoiceWaveform(
    rmsLevel: Float,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 5,
    activeColor: Color = Color(0xFF00F0FF),
    inactiveColor: Color = Color(0xFF475569)
) {
    val transition = rememberInfiniteTransition(label = "voice_wave")
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 6.283f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave_phase"
    )

    Canvas(modifier = modifier) {
        val spacing = size.width / (barCount * 2f)
        val barWidth = spacing.coerceAtLeast(2f)
        for (i in 0 until barCount) {
            val dynamicFactor = if (isActive) {
                val wave = ((sin(phase + i * 0.9f) + 1f) * 0.5f)
                (0.25f + (rmsLevel.coerceIn(0.05f, 1f) * 0.75f * wave)).coerceIn(0.2f, 1f)
            } else {
                0.25f
            }
            val barHeight = size.height * dynamicFactor
            val x = i * (barWidth + spacing) + spacing / 2f
            val y = (size.height - barHeight) / 2f
            drawRoundRect(
                color = if (isActive) activeColor else inactiveColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2f, barWidth / 2f)
            )
        }
    }
}
