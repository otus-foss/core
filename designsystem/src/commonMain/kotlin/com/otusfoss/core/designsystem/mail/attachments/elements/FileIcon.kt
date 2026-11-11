package com.otusfoss.core.designsystem.mail.attachments.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.otusfoss.core.designsystem.theme.tokens.content.FileKindTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle

/**
 * A file as a picture, [size] tall: its [picture] with round corners when it has one, else a page
 * with a folded corner in the [tint] of its kind and its type, the [label], written on it, such as
 * "PDF". The label tells kinds apart without their color. It is decoration: the name beside it
 * says what the file is.
 */
@Composable
public fun FileIcon(
    label: String,
    tint: Color,
    size: Dp,
    modifier: Modifier = Modifier,
    picture: ImageBitmap? = null,
) {
    Box(modifier.size(size), contentAlignment = Alignment.Center) {
        if (picture != null) {
            Image(
                picture,
                contentDescription = null,
                Modifier.fillMaxSize().clip(RoundedCornerShape(FileKindTokens.pictureRadius)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                Modifier.fillMaxHeight().aspectRatio(FileKindTokens.PAGE_RATIO).drawBehind { page(tint) },
                contentAlignment = Alignment.BottomCenter,
            ) {
                val labelSize = (size.value * FileKindTokens.LABEL_SIZE).sp
                Text(
                    label,
                    Modifier.padding(bottom = size * FileKindTokens.LABEL_INSET).clearAndSetSemantics {},
                    style = panelTextStyle(),
                    color = FileKindTokens.label,
                    fontSize = labelSize,
                    lineHeight = labelSize,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                )
            }
        }
    }
}

/** The page: round corners, except the top end one, which is cut and folded over. */
private fun DrawScope.page(tint: Color) {
    val width = size.width
    val height = size.height
    val radius = width * FileKindTokens.CORNER
    val fold = width * FileKindTokens.FOLD
    drawPath(
        Path().apply {
            moveTo(radius, 0f)
            lineTo(width - fold, 0f)
            lineTo(width, fold)
            lineTo(width, height - radius)
            quadraticTo(width, height, width - radius, height)
            lineTo(radius, height)
            quadraticTo(0f, height, 0f, height - radius)
            lineTo(0f, radius)
            quadraticTo(0f, 0f, radius, 0f)
            close()
        },
        tint,
    )
    drawPath(
        Path().apply {
            moveTo(width - fold, 0f)
            lineTo(width, fold)
            lineTo(width - fold, fold)
            close()
        },
        FileKindTokens.label,
        alpha = FileKindTokens.FOLD_ALPHA,
    )
}
