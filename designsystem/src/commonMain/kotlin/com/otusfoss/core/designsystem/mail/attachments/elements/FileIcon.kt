package com.otusfoss.core.designsystem.mail.attachments.elements

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp
import com.otusfoss.core.designsystem.shared.controls.elements.Symbol
import com.otusfoss.core.designsystem.theme.tokens.content.FileKindTokens
import com.otusfoss.core.designsystem.theme.typography.panelTextStyle
import org.jetbrains.compose.resources.DrawableResource

/**
 * A file as a picture, [size] tall: its [picture] with round corners when it has one, else its
 * kind's page [icon] with its type, the [label], written in the lower part of the page, such as
 * "PDF". It is decoration: the name beside it says what the file is.
 */
@Composable
public fun FileIcon(
    icon: DrawableResource,
    label: String,
    size: Dp,
    modifier: Modifier = Modifier,
    picture: ImageBitmap? = null,
) {
    Box(modifier.size(size), contentAlignment = Alignment.BottomCenter) {
        if (picture != null) {
            Image(
                picture,
                contentDescription = null,
                Modifier.fillMaxSize().clip(RoundedCornerShape(FileKindTokens.pictureRadius)),
                contentScale = ContentScale.Crop,
            )
        } else {
            Symbol(icon, size = size)
            val labelSize = (size.value * FileKindTokens.LABEL_SIZE).sp
            Text(
                label,
                Modifier.padding(bottom = size * FileKindTokens.LABEL_INSET).clearAndSetSemantics {},
                style = panelTextStyle(),
                fontSize = labelSize,
                lineHeight = labelSize,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}
