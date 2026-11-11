package io.music_assistant.client.ui.compose.home.players

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
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
    color: Color,
    iconColor: Color,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    // One revolution in 1800 ms approximates 33 1/3 RPM.
    val rotation = remember { Animatable(0f) }
    val trackSlide = remember { Animatable(0f) }
    var lastTitle by remember { androidx.compose.runtime.mutableStateOf<String?>(null) }
    var swipeDirection by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(title) {
        if (lastTitle != null && title != lastTitle) {
            trackSlide.snapTo(65f * swipeDirection)
            trackSlide.animateTo(0f, tween(durationMillis = 330))
        }
        lastTitle = title
    }
    LaunchedEffect(playing) {
        while (playing) {
            rotation.animateTo(
                rotation.value + 360f,
                animationSpec = tween(durationMillis = 1800, easing = LinearEasing),
            )
            rotation.snapTo(rotation.value % 360f)
        }
    }
    val armProgress by animateFloatAsState(
        targetValue = if (playing) 1f else 0f,
        animationSpec = tween(durationMillis = 450),
        label = "tonearm",
    )
    val thresholdPx = with(LocalDensity.current) { 85.dp.toPx() }
    val density = LocalDensity.current.density
    var swipeDistance by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(onPrevious, onNext, thresholdPx) {
                detectHorizontalDragGestures(
                    onDragStart = { swipeDistance = 0f },
                    onHorizontalDrag = { change, amount ->
                        swipeDistance += amount
                        change.consume()
                    },
                    onDragEnd = {
                        if (swipeDistance > thresholdPx) {
                            swipeDirection = -1f
                            onPrevious()
                        }
                        if (swipeDistance < -thresholdPx) {
                            swipeDirection = 1f
                            onNext()
                        }
                        swipeDistance = 0f
                    },
                    onDragCancel = { swipeDistance = 0f },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(0.90f)
                .graphicsLayer {
                    rotationZ = rotation.value
                    translationX = trackSlide.value * density
                },
            contentAlignment = Alignment.Center,
        ) {
            Canvas(Modifier.fillMaxSize()) {
                val radius = size.minDimension * 0.49f
                drawCircle(Color(0xFF090B10), radius = radius)
                drawCircle(Color(0xFF242B35), radius = radius * 0.97f)
                drawCircle(Color(0xFF10141B), radius = radius * 0.94f)
                for (n in 0..17) {
                    drawCircle(
                        color = if (n % 3 == 0) Color(0xFF656D78) else Color(0xFF454A54),
                        radius = radius * (0.55f + n * 0.022f),
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
                drawCircle(Color(0xFF2C323C), radius = radius * 0.46f)
            }
            val placeholder = rememberPlaceholderPainter(
                backgroundColor = color,
                iconColor = iconColor,
                icon = icon,
            )
            Box(
                modifier = Modifier.fillMaxSize(0.42f).clip(CircleShape),
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
            val pivot = Offset(size.width * 0.94f, size.height * 0.08f)
            // Resting stylus is outside the record; move smoothly onto its grooves.
            val restElbow = Offset(size.width * 0.98f, size.height * 0.43f)
            val playElbow = Offset(size.width * 0.89f, size.height * 0.42f)
            val restTip = Offset(size.width * 0.96f, size.height * 0.67f)
            val playTip = Offset(size.width * 0.73f, size.height * 0.55f)
            fun between(a: Offset, b: Offset) = Offset(
                a.x + (b.x - a.x) * armProgress,
                a.y + (b.y - a.y) * armProgress,
            )
            val elbow = between(restElbow, playElbow)
            val tip = between(restTip, playTip)
            drawCircle(Color(0xFF8A929F), radius = size.minDimension * 0.052f, center = pivot)
            drawCircle(Color(0xFFD6DCE3), radius = size.minDimension * 0.029f, center = pivot)
            drawLine(Color(0xFF222833), pivot, elbow, strokeWidth = size.minDimension * 0.036f, cap = StrokeCap.Round)
            drawLine(Color(0xFFCDD2D9), pivot, elbow, strokeWidth = size.minDimension * 0.023f, cap = StrokeCap.Round)
            drawLine(Color(0xFFCDD2D9), elbow, tip, strokeWidth = size.minDimension * 0.022f, cap = StrokeCap.Round)
            drawCircle(Color(0xFF292E39), radius = size.minDimension * 0.027f, center = tip)
        }
    }
}
