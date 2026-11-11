package com.example.counter.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** "+" or "-" drawn with round caps so it reads like a real icon, not a text glyph. */
@Composable
internal fun StepGlyph(plus: Boolean, color: Color, size: Dp = 24.dp) {
    Canvas(Modifier.size(size)) {
        val stroke = this.size.minDimension * 0.13f
        val edge = this.size.minDimension * 0.2f
        val far = this.size.minDimension - edge
        val center = this.size.minDimension / 2f
        drawLine(color, Offset(edge, center), Offset(far, center), stroke, StrokeCap.Round)
        if (plus) {
            drawLine(color, Offset(center, edge), Offset(center, far), stroke, StrokeCap.Round)
        }
    }
}

@Composable
internal fun DotsGlyph(color: Color, size: Dp = 18.dp) {
    Canvas(Modifier.size(size)) {
        val radius = this.size.minDimension * 0.11f
        val y = this.size.height / 2f
        listOf(0.2f, 0.5f, 0.8f).forEach { fraction ->
            drawCircle(color = color, radius = radius, center = Offset(this.size.width * fraction, y))
        }
    }
}

@Composable
internal fun TrashGlyph(color: Color, size: Dp = 24.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.085f
        val cap = StrokeCap.Round
        drawLine(color, Offset(w * 0.14f, h * 0.26f), Offset(w * 0.86f, h * 0.26f), stroke, cap)
        drawLine(color, Offset(w * 0.38f, h * 0.14f), Offset(w * 0.62f, h * 0.14f), stroke, cap)
        val body = Path().apply {
            moveTo(w * 0.22f, h * 0.34f)
            lineTo(w * 0.27f, h * 0.84f)
            quadraticTo(w * 0.28f, h * 0.9f, w * 0.34f, h * 0.9f)
            lineTo(w * 0.66f, h * 0.9f)
            quadraticTo(w * 0.72f, h * 0.9f, w * 0.73f, h * 0.84f)
            lineTo(w * 0.78f, h * 0.34f)
        }
        drawPath(body, color, style = Stroke(width = stroke, cap = cap, join = StrokeJoin.Round))
        drawLine(color, Offset(w * 0.42f, h * 0.46f), Offset(w * 0.44f, h * 0.76f), stroke * 0.8f, cap)
        drawLine(color, Offset(w * 0.58f, h * 0.46f), Offset(w * 0.56f, h * 0.76f), stroke * 0.8f, cap)
    }
}

/**
 * Shows the layout you would switch *to*: an outlined pair of cards, or a list with lead dots.
 */
@Composable
internal fun LayoutGlyph(showCompact: Boolean, color: Color, size: Dp = 24.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        if (showCompact) {
            // Dense list: three chunky outlined bars, same ink weight as the card variant.
            val stroke = h * 0.08f
            listOf(0.1f to 0.22f, 0.39f to 0.22f, 0.68f to 0.22f).forEach { (top, height) ->
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.12f, h * top),
                    size = Size(w * 0.76f, h * height),
                    cornerRadius = CornerRadius(h * 0.08f, h * 0.08f),
                    style = Stroke(width = stroke),
                )
            }
        } else {
            // Outlined stacked cards.
            val stroke = h * 0.075f
            listOf(0.14f to 0.3f, 0.56f to 0.3f).forEach { (top, height) ->
                drawRoundRect(
                    color = color,
                    topLeft = Offset(w * 0.14f, h * top),
                    size = Size(w * 0.72f, h * height),
                    cornerRadius = CornerRadius(h * 0.09f, h * 0.09f),
                    style = Stroke(width = stroke),
                )
            }
        }
    }
}

/** Circular arrow used for "reset all". */
@Composable
internal fun ResetGlyph(color: Color, size: Dp = 24.dp) {
    Canvas(Modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = w * 0.09f
        val radius = w * 0.33f
        val center = Offset(w / 2f, h / 2f)
        val startAngle = 62f
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = 288f,
            useCenter = false,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2f, radius * 2f),
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )
        // Arrow head, pointing along the arc's direction of travel at the start of the sweep.
        val rad = startAngle * PI.toFloat() / 180f
        val tip = Offset(
            center.x + radius * cos(rad),
            center.y + radius * sin(rad),
        )
        val tangent = Offset(-sin(rad), cos(rad))
        val barb = w * 0.19f
        fun rotate(vector: Offset, degrees: Float): Offset {
            val a = degrees * PI.toFloat() / 180f
            return Offset(
                vector.x * cos(a) - vector.y * sin(a),
                vector.x * sin(a) + vector.y * cos(a),
            )
        }

        val back = rotate(tangent, 180f)
        listOf(38f, -38f).forEach { spread ->
            val barbEnd = rotate(back, spread) * barb + tip
            drawLine(
                color = color,
                start = tip,
                end = barbEnd,
                strokeWidth = stroke,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Little pin mark used to flag a pinned counter. */
@Composable
internal fun PinGlyph(color: Color, size: Dp = 16.dp) {
    Canvas(Modifier.size(size)) {
        val s = this.size.minDimension
        drawCircle(color = color, radius = s * 0.26f, center = Offset(s / 2f, s * 0.34f))
        drawLine(
            color = color,
            start = Offset(s / 2f, s * 0.56f),
            end = Offset(s / 2f, s * 0.9f),
            strokeWidth = s * 0.13f,
            cap = StrokeCap.Round,
        )
    }
}