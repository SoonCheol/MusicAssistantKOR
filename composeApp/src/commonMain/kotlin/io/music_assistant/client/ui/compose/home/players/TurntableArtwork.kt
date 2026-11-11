package io.music_assistant.client.ui.compose.home.players

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import io.music_assistant.client.imageloader.rememberArtworkRequest
import io.music_assistant.client.ui.compose.common.painters.rememberPlaceholderPainter

/**
 * Visual-only KOR artwork replacement. Deliberately scoped to the artwork box:
 * the outer player pager (other output devices), vertical queue gestures,
 * transport controls and lyrics interaction remain unchanged.
 */
@Composable
internal fun TurntableArtwork(
    imageUrl: String?,
    title: String?,
    icon: ImageVector,
    playing: Boolean,
    trackKey: Pair<String?, String?>,
    color: Color,
    iconColor: Color,
) {
    // 12 RPM (one revolution every 5 seconds), shared by vinyl and album image.
    var rotationDegrees by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(playing) {
        if (playing) {
            var previousFrameNanos = 0L
            while (true) {
                withFrameNanos { frameNanos ->
                    if (previousFrameNanos != 0L) {
                        val elapsedSeconds = ((frameNanos - previousFrameNanos) / 1_000_000_000f)
                            .coerceIn(0f, 0.05f)
                        // 12 RPM = 72 degrees per second.
                        rotationDegrees = (rotationDegrees + elapsedSeconds * 72f) % 360f
                    }
                    previousFrameNanos = frameNanos
                }
            }
        }
    }
    // On an actively playing track change, lift the arm briefly before lowering it
    // onto the new cover. Paused or stopped tracks keep the arm parked.
    var armEngaged by remember { androidx.compose.runtime.mutableStateOf(playing) }
    LaunchedEffect(trackKey, playing) {
        if (!playing) {
            armEngaged = false
        } else {
            armEngaged = false
            delay(450)
            armEngaged = true
        }
    }
    val armProgress by animateFloatAsState(
        targetValue = if (armEngaged) 1f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "tonearm",
    )
    // Artwork is display-only; track swipes are recognized exclusively below transport.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.92f)
                .graphicsLayer {
                    rotationZ = rotationDegrees
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension * 0.49f
                drawCircle(Color(0xFF718A94), radius = radius)
                drawCircle(Color(0xFF050608), radius = radius * 0.94f)
                drawCircle(Color(0xFF121417), radius = radius * 0.90f)
                for (n in 0..8) {
                    drawCircle(
                        color = if (n % 3 == 0) Color(0xFF656D78) else Color(0xFF454A54),
                        radius = radius * (0.64f + n * 0.027f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 0.8f),
                        alpha = if (n % 3 == 0) 0.35f else 0.18f,
                    )
                }
                // Directional groove reflection reveals movement on uniform artwork.
                drawArc(
                    color = Color(0xFFB3BBC9),
                    startAngle = -55f,
                    sweepAngle = 43f,
                    useCenter = false,
                    style = Stroke(width = radius * 0.018f, cap = StrokeCap.Round),
                    alpha = 0.50f,
                    topLeft = Offset(center.x - radius * 0.87f, center.y - radius * 0.87f),
                    size = androidx.compose.ui.geometry.Size(radius * 1.74f, radius * 1.74f),
                )
                drawCircle(Color(0xFF111318), radius = radius * 0.64f)
            }
            val placeholder = rememberPlaceholderPainter(
                backgroundColor = color,
                iconColor = iconColor,
                icon = icon,
            )
            Box(
                modifier = Modifier.fillMaxSize(0.60f).clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                if (imageUrl != null) {
                    AsyncImage(
                        model = rememberArtworkRequest(imageUrl),
                        contentDescription = title,
                        placeholder = placeholder,
                        error = placeholder,
                        fallback = placeholder,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    Canvas(Modifier.fillMaxSize()) { drawCircle(color) }
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = iconColor,
                        modifier = Modifier.fillMaxSize(0.48f),
                    )
                }
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color(0xFFD2D7DE), radius = size.minDimension * 0.036f)
                }
            }
        }
        Canvas(Modifier.fillMaxSize()) {
            // UGREEN reference: ivory curved tonearm with a small light cartridge,
            // anchored above/right of the record. Keep the arm stationary while
            // the vinyl artwork rotates beneath it.
            val scale = size.minDimension
            val pivot = Offset(size.width * 0.86f, size.height * 0.055f)
            val restingBend = Offset(size.width * 0.977f, size.height * 0.405f)
            val playingBend = Offset(size.width * 0.845f, size.height * 0.385f)
            val restingTip = Offset(size.width * 0.943f, size.height * 0.487f)
            val playingTip = Offset(size.width * 0.765f, size.height * 0.482f)
            fun between(a: Offset, b: Offset) = Offset(
                a.x + (b.x - a.x) * armProgress,
                a.y + (b.y - a.y) * armProgress,
            )
            val bend = between(restingBend, playingBend)
            val tip = between(restingTip, playingTip)
            val armPath = Path().apply {
                moveTo(pivot.x + scale * 0.008f, pivot.y + scale * 0.025f)
                lineTo(bend.x - scale * 0.019f, bend.y - scale * 0.055f)
                quadraticBezierTo(
                    bend.x + scale * 0.008f, bend.y - scale * 0.012f,
                    bend.x, bend.y + scale * 0.018f,
                )
                lineTo(tip.x, tip.y)
            }
            // Soft grey edge gives the light, rounded rod enough depth on
            // both pale and dark album-derived backgrounds.
            drawPath(
                armPath,
                color = Color(0xFF9BA7AA),
                style = Stroke(width = scale * 0.027f, cap = StrokeCap.Round),
            )
            drawPath(
                armPath,
                color = Color(0xFFF0F1EF),
                style = Stroke(width = scale * 0.021f, cap = StrokeCap.Round),
            )
            drawPath(
                armPath,
                color = Color(0xFFFFFFFF).copy(alpha = 0.47f),
                style = Stroke(width = scale * 0.006f, cap = StrokeCap.Round),
            )
            // Rounded, light pickup head: no black ball or sharp elbow.
            rotate(degrees = -24f - armProgress * 15f, pivot = tip) {
                drawRoundRect(
                    color = Color(0xFF9AA6A9),
                    topLeft = Offset(tip.x - scale * 0.019f, tip.y - scale * 0.008f),
                    size = Size(scale * 0.043f, scale * 0.085f),
                    cornerRadius = CornerRadius(scale * 0.007f),
                )
                drawRoundRect(
                    color = Color(0xFFE9EBE9),
                    topLeft = Offset(tip.x - scale * 0.019f, tip.y - scale * 0.012f),
                    size = Size(scale * 0.038f, scale * 0.080f),
                    cornerRadius = CornerRadius(scale * 0.007f),
                )
            }
            drawCircle(Color(0xFF9EA9AC), radius = scale * 0.043f, center = pivot)
            drawCircle(Color(0xFFE1E6E7), radius = scale * 0.036f, center = pivot)
            drawCircle(Color(0xFFBCC7C9), radius = scale * 0.025f, center = pivot)
            drawCircle(Color(0xFFD6DCDD), radius = scale * 0.020f, center = pivot)
        }
    }
}
