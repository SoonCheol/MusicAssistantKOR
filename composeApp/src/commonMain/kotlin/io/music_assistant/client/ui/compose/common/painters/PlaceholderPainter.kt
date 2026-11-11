// Drawing geometry / hex color literals — values are inherent to the painter's visual design.
@file:Suppress("MagicNumber")

package io.music_assistant.client.ui.compose.common.painters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter

private class PlaceholderPainter(
    private val backgroundColor: Color = Color(0xFFE8E8E8),
    private val iconColor: Color = Color(0xFF9E9E9E),
    private val iconPainter: Painter,
) : Painter() {
    override val intrinsicSize: Size = Size.Unspecified

    override fun DrawScope.onDraw() {
        val canvasWidth = size.width
        val canvasHeight = size.height

        // Unified KOR placeholder for missing album/artist/track/podcast art.
        // The existing caller supplies the type-specific vector (record,
        // person, note, microphone); real artwork is never replaced.
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
        val base = minOf(canvasWidth, canvasHeight)
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF153247), Color(0xFF28566A), Color(0xFF10283C)),
                start = Offset.Zero,
                end = Offset(canvasWidth, canvasHeight),
            ),
            size = Size(canvasWidth, canvasHeight),
        )
        // Keep a trace of the track/player-derived color without changing
        // the recognizable navy KOR visual identity.
        drawCircle(
            color = backgroundColor.copy(alpha = 0.12f),
            radius = base * 0.55f,
            center = center,
        )
        drawCircle(
            color = Color(0xFFE1EEF1),
            radius = base * 0.36f,
            center = center,
            alpha = 0.12f,
        )
        drawCircle(
            color = Color(0xFFA9CFD6),
            radius = base * 0.36f,
            center = center,
            alpha = 0.53f,
            style = Stroke(width = base * 0.012f),
        )
        drawCircle(
            color = Color(0xFFB9D9DF),
            radius = base * 0.28f,
            center = center,
            alpha = 0.21f,
            style = Stroke(width = base * 0.0035f),
        )

        val iconSize = base * 0.40f
        translate(left = center.x - iconSize / 2f, top = center.y - iconSize / 2f) {
            with(iconPainter) {
                draw(
                    size = Size(iconSize, iconSize),
                    colorFilter = ColorFilter.tint(Color(0xFFE8F5F6)),
                )
            }
        }
    }
}

@Composable
fun rememberPlaceholderPainter(
    backgroundColor: Color = Color(0xFFE8E8E8),
    iconColor: Color = Color(0xFF9E9E9E),
    icon: ImageVector,
): Painter {
    val vectorPainter = rememberVectorPainter(icon)
    return remember(backgroundColor, iconColor, vectorPainter) {
        PlaceholderPainter(
            backgroundColor = backgroundColor,
            iconColor = iconColor,
            iconPainter = vectorPainter,
        )
    }
}
