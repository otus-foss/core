package com.otus.designsystem.theme.shapes

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.toRect
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.otus.designsystem.theme.tokens.layout.CornerTokens
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * A rounded rectangle whose corners ease into its edges the way Figma's corner smoothing draws
 * them, so the curvature never jumps where an edge meets a corner (G2). A corner as large as half
 * the shorter side has no room to ease and stays a plain arc, so pills keep their round ends.
 */
public class SmoothCornerShape(
    topStart: CornerSize,
    topEnd: CornerSize,
    bottomEnd: CornerSize,
    bottomStart: CornerSize,
) : CornerBasedShape(topStart, topEnd, bottomEnd, bottomStart) {
    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize,
    ): SmoothCornerShape = SmoothCornerShape(topStart, topEnd, bottomEnd, bottomStart)

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection,
    ): Outline {
        if (topStart + topEnd + bottomEnd + bottomStart == 0f) return Outline.Rectangle(size.toRect())
        val ltr = layoutDirection == LayoutDirection.Ltr
        val budget = size.minDimension / 2
        val right = Offset(1f, 0f)
        val down = Offset(0f, 1f)
        return Outline.Generic(
            Path().apply {
                smoothCorner(Offset(size.width, 0f), right, down, if (ltr) topEnd else topStart, budget)
                smoothCorner(Offset(size.width, size.height), down, -right, if (ltr) bottomEnd else bottomStart, budget)
                smoothCorner(Offset(0f, size.height), -right, -down, if (ltr) bottomStart else bottomEnd, budget)
                smoothCorner(Offset.Zero, -down, right, if (ltr) topStart else topEnd, budget)
                close()
            },
        )
    }

    override fun equals(other: Any?): Boolean =
        this === other ||
            (
                other is SmoothCornerShape &&
                    topStart == other.topStart &&
                    topEnd == other.topEnd &&
                    bottomEnd == other.bottomEnd &&
                    bottomStart == other.bottomStart
            )

    override fun hashCode(): Int = listOf(topStart, topEnd, bottomEnd, bottomStart).hashCode()
}

public fun SmoothCornerShape(size: Dp): SmoothCornerShape = SmoothCornerShape(CornerSize(size))

/** A shape with every corner [size] pixels, for corners animated in a graphics layer. */
public fun SmoothCornerShape(size: Float): SmoothCornerShape = SmoothCornerShape(CornerSize(size))

public fun SmoothCornerShape(
    topStart: Dp = 0.dp,
    topEnd: Dp = 0.dp,
    bottomEnd: Dp = 0.dp,
    bottomStart: Dp = 0.dp,
): SmoothCornerShape =
    SmoothCornerShape(CornerSize(topStart), CornerSize(topEnd), CornerSize(bottomEnd), CornerSize(bottomStart))

/** This shape's outline drawn over [rect], for surfaces that draw their own path. */
internal fun Shape.toPath(
    rect: Rect,
    layoutDirection: LayoutDirection,
    density: Density,
): Path =
    Path().apply {
        addOutline(createOutline(rect.size, layoutDirection, density))
        translate(rect.topLeft)
    }

private fun SmoothCornerShape(corner: CornerSize): SmoothCornerShape = SmoothCornerShape(corner, corner, corner, corner)

/**
 * Draws one corner, travelling along [incoming] into [corner] and leaving along [outgoing]. The
 * lengths follow Figma's corner smoothing: two easing curves around a shorter circular arc, all
 * within [budget], half the shape's shorter side. The arc is drawn as a cubic too.
 */
private fun Path.smoothCorner(
    corner: Offset,
    incoming: Offset,
    outgoing: Offset,
    radius: Float,
    budget: Float,
) {
    val r = radius.coerceAtMost(budget)
    if (r <= 0f) return moveOrLineTo(corner)
    val smoothing = CornerTokens.SMOOTHING.coerceAtMost(budget / r - 1)
    val reach = min((1 + smoothing) * r, budget)
    val arcDegrees = 90f * (1 - smoothing)
    val arcLength = sin(radians(arcDegrees / 2)) * r * sqrt(2f)
    val easeDegrees = 45f * smoothing
    val c = r * tan(radians((90f - arcDegrees) / 4)) * cos(radians(easeDegrees))
    val d = c * tan(radians(easeDegrees))
    val b = (reach - arcLength - c - d) / 3
    val a = 2 * b
    val start = corner - incoming * reach

    fun point(
        along: Float,
        across: Float,
    ) = start + incoming * along + outgoing * across
    val arcStart = point(a + b + c, d)
    val arcEnd = point(reach - d, reach - a - b - c)
    val handle = 4f / 3 * tan(radians(arcDegrees / 4)) * r
    val easeCos = cos(radians(easeDegrees))
    val easeSin = sin(radians(easeDegrees))
    moveOrLineTo(start)
    cubicTo(point(a, 0f), point(a + b, 0f), arcStart)
    cubicTo(
        arcStart + (incoming * easeCos + outgoing * easeSin) * handle,
        arcEnd - (incoming * easeSin + outgoing * easeCos) * handle,
        arcEnd,
    )
    cubicTo(point(reach, reach - a - b), point(reach, reach - a), point(reach, reach))
}

private fun Path.moveOrLineTo(point: Offset) = if (isEmpty) moveTo(point.x, point.y) else lineTo(point.x, point.y)

private fun Path.cubicTo(
    first: Offset,
    second: Offset,
    end: Offset,
) = cubicTo(first.x, first.y, second.x, second.y, end.x, end.y)

private fun radians(degrees: Float): Float = degrees * PI.toFloat() / 180
