package com.example.ui.theme

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pure White Daylight Sovereign OS Background & Surface Decorators.
 */
fun Modifier.cyberAmbientBackground(
    showGrid: Boolean = true,
    primaryGlow: Color = CyberCyan,
    secondaryGlow: Color = CyberPurple
): Modifier = this
    .background(Color.White)
    .drawBehind {
        // 1. Crisp Pure White & Soft Daylight Porcelain Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFFFFF),
                    Color(0xFFF8FAFC),
                    Color(0xFFFFFFFF)
                )
            )
        )

        // 2. Subtle Daylight Sky Accent
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryGlow.copy(alpha = 0.05f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.15f, size.height * 0.10f),
                radius = size.width * 0.70f
            ),
            radius = size.width * 0.70f,
            center = Offset(size.width * 0.15f, size.height * 0.10f)
        )

        // 3. Subtle Daylight Emerald Accent
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    CyberEmerald.copy(alpha = 0.04f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.85f, size.height * 0.82f),
                radius = size.width * 0.65f
            ),
            radius = size.width * 0.65f,
            center = Offset(size.width * 0.85f, size.height * 0.82f)
        )

        // 4. Delicate Precision Grid Lines on White Canvas
        if (showGrid) {
            val stepPx = 38.dp.toPx()
            val gridColor = Color(0xFFE2E8F0).copy(alpha = 0.45f)
            var x = 0f
            while (x < size.width) {
                drawLine(
                    color = gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 0.8f
                )
                x += stepPx
            }
            var y = 0f
            while (y < size.height) {
                drawLine(
                    color = gridColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 0.8f
                )
                y += stepPx
            }
        }
    }

@Composable
fun CyberRecessedSlot(
    modifier: Modifier = Modifier,
    accentColor: Color = CyberCyan,
    cornerRadius: Dp = 12.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val radiusPx = cornerRadius
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF8FAFC),
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFCBD5E1),
                        accentColor.copy(alpha = 0.55f)
                    )
                ),
                shape = RoundedCornerShape(cornerRadius)
            )
            .drawBehind {
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    style = Stroke(width = 1.5.dp.toPx()),
                    cornerRadius = CornerRadius(radiusPx.toPx(), radiusPx.toPx())
                )
            },
        content = content
    )
}

@Composable
fun CyberGlassCard(
    modifier: Modifier = Modifier,
    accentColor: Color = CyberCyan,
    cornerRadius: Dp = 14.dp,
    contentPadding: Dp = 12.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        color = Color(0xFFFFFFFF),
        shape = RoundedCornerShape(cornerRadius),
        border = BorderStroke(
            width = 1.dp,
            brush = Brush.horizontalGradient(
                colors = listOf(
                    accentColor.copy(alpha = 0.65f),
                    accentColor.copy(alpha = 0.30f),
                    CyberPurple.copy(alpha = 0.45f)
                )
            )
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            content = content
        )
    }
}

@Composable
fun CyberQuickActionChip(
    label: String,
    accentColor: Color = CyberCyan,
    isActive: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        color = if (isActive) accentColor.copy(alpha = 0.16f) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(9.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) accentColor else accentColor.copy(alpha = 0.55f)
        ),
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isActive) Color(0xFF0F172A) else accentColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
        )
    }
}

@Composable
fun CyberLedBadge(
    label: String,
    color: Color = CyberEmerald,
    isPulsingOrActive: Boolean = true
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFFF8FAFC))
            .border(0.8.dp, color.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isPulsingOrActive) color else Color.Gray)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = label,
            color = if (isPulsingOrActive) color else Color.Gray,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace
        )
    }
}
