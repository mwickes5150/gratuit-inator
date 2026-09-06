package com.mwickes.gratuitinator.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Thin-stroke sun glyph (circle + rays) — an outline replacement for the sun/moon emoji, matching
 * the Paper Tape aesthetic's line-art style instead of a colorful platform emoji.
 */
@Composable
fun SunOutlineIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.6.dp.toPx()
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val radius = this.size.minDimension * 0.28f
        drawCircle(color = color, radius = radius, center = center, style = Stroke(width = strokeWidth))

        val rayInner = radius * 1.35f
        val rayOuter = radius * 1.9f
        for (i in 0 until 8) {
            val angle = Math.toRadians((i * 45).toDouble())
            val dx = cos(angle).toFloat()
            val dy = sin(angle).toFloat()
            drawLine(
                color = color,
                start = Offset(center.x + dx * rayInner, center.y + dy * rayInner),
                end = Offset(center.x + dx * rayOuter, center.y + dy * rayOuter),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round,
            )
        }
    }
}

/** Thin-stroke crescent-moon glyph, matching [SunOutlineIcon]'s line weight. */
@Composable
fun MoonOutlineIcon(color: Color, modifier: Modifier = Modifier, size: Dp = 20.dp) {
    Canvas(modifier = modifier.size(size)) {
        val strokeWidth = 1.6.dp.toPx()
        val radius = this.size.minDimension * 0.32f
        val center = Offset(this.size.width / 2f, this.size.height / 2f)

        val outer = Path().apply { addOval(Rect(center = center, radius = radius)) }
        val cutoutCenter = Offset(center.x + radius * 0.55f, center.y - radius * 0.35f)
        val inner = Path().apply { addOval(Rect(center = cutoutCenter, radius = radius * 0.85f)) }
        val crescent = Path().apply { op(outer, inner, PathOperation.Difference) }

        drawPath(path = crescent, color = color, style = Stroke(width = strokeWidth))
    }
}
