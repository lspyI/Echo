package com.example.echojournal.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A modifier that draws a glass background behind the content.
 * This avoids blurring the content (text, icons) itself.
 */
fun Modifier.glassmorphism(
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    backgroundColor: Color = Color.White.copy(alpha = 0.05f),
    isLiquid: Boolean = false
): Modifier = this
    .clip(shape)
    .background(
        Brush.verticalGradient(
            colors = if (isLiquid) {
                listOf(
                    backgroundColor.copy(alpha = backgroundColor.alpha * 2.5f),
                    backgroundColor.copy(alpha = backgroundColor.alpha * 0.2f)
                )
            } else {
                listOf(
                    backgroundColor.copy(alpha = backgroundColor.alpha * 1.5f),
                    backgroundColor
                )
            }
        )
    )
    .border(
        width = if (isLiquid) 1.dp else 0.5.dp,
        brush = Brush.linearGradient(
            listOf(
                borderColor.copy(alpha = if (isLiquid) 0.6f else 0.2f),
                borderColor.copy(alpha = 0.05f)
            )
        ),
        shape = shape
    )

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    borderColor: Color = Color.White.copy(alpha = 0.15f),
    isLiquid: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier.glassmorphism(
            shape = shape, 
            borderColor = borderColor,
            isLiquid = isLiquid
        )
    ) {
        content()
    }
}

fun Modifier.glassIcon(
    isLiquid: Boolean = false,
    backgroundColor: Color = Color.White.copy(alpha = 0.05f)
): Modifier = this
    .clip(CircleShape)
    .background(
        Brush.radialGradient(
            colors = if (isLiquid) {
                listOf(backgroundColor.copy(alpha = 0.3f), backgroundColor.copy(alpha = 0.05f))
            } else {
                listOf(backgroundColor.copy(alpha = 0.15f), backgroundColor.copy(alpha = 0.05f))
            }
        )
    )
    .border(0.5.dp, Color.White.copy(alpha = if (isLiquid) 0.4f else 0.1f), CircleShape)
    .padding(8.dp)

fun Modifier.glow(
    color: Color = Color.White,
    alpha: Float = 0.2f,
    radius: Dp = 12.dp
): Modifier = this.drawBehind {
    val radiusPx = radius.toPx()
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(color.copy(alpha = alpha), Color.Transparent),
            center = center,
            radius = radiusPx
        ),
        radius = radiusPx,
        center = center
    )
}
