package com.otusfoss.core.designsystem.shared.controls.elements

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorGroup
import androidx.compose.ui.graphics.vector.VectorNode
import androidx.compose.ui.graphics.vector.VectorPath

/**
 * A copy of this vector painted in real colors: paths drawn at full alpha take [front], the lighter
 * back paths take [back], and both become opaque. Shapes and edges stay the same.
 */
internal fun ImageVector.painted(
    front: Color,
    back: Color,
): ImageVector =
    ImageVector
        .Builder(
            name = name,
            defaultWidth = defaultWidth,
            defaultHeight = defaultHeight,
            viewportWidth = viewportWidth,
            viewportHeight = viewportHeight,
            tintColor = tintColor,
            tintBlendMode = tintBlendMode,
            autoMirror = autoMirror,
        ).apply { root.forEach { addPainted(it, front, back) } }
        .build()

private fun ImageVector.Builder.addPainted(
    node: VectorNode,
    front: Color,
    back: Color,
) {
    when (node) {
        is VectorGroup -> {
            addGroup(
                name = node.name,
                rotate = node.rotation,
                pivotX = node.pivotX,
                pivotY = node.pivotY,
                scaleX = node.scaleX,
                scaleY = node.scaleY,
                translationX = node.translationX,
                translationY = node.translationY,
                clipPathData = node.clipPathData,
            )
            node.forEach { addPainted(it, front, back) }
            clearGroup()
        }

        is VectorPath -> {
            addPath(
                pathData = node.pathData,
                pathFillType = node.pathFillType,
                name = node.name,
                fill = SolidColor(if (node.fillAlpha < 1f) back else front),
                fillAlpha = 1f,
                stroke = node.stroke,
                strokeAlpha = node.strokeAlpha,
                strokeLineWidth = node.strokeLineWidth,
                strokeLineCap = node.strokeLineCap,
                strokeLineJoin = node.strokeLineJoin,
                strokeLineMiter = node.strokeLineMiter,
                trimPathStart = node.trimPathStart,
                trimPathEnd = node.trimPathEnd,
                trimPathOffset = node.trimPathOffset,
            )
        }
    }
}
