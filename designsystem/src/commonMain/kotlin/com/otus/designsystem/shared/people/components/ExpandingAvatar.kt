package com.otus.designsystem.shared.people.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.lerp
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.lerp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import com.otus.designsystem.shared.people.elements.rememberAvatarImage
import com.otus.designsystem.theme.MotionCurve
import com.otus.designsystem.theme.MotionSpeed
import com.otus.designsystem.theme.OtusColors
import com.otus.designsystem.theme.PageTokens
import com.otus.designsystem.theme.SheetTokens
import com.otus.designsystem.theme.motionTween
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * [avatar] at [size]. When it has a photo, a tap grows the photo from where it sits to the width
 * of the screen over a scrim, rounding its circle into a card; a tap or back shrinks it home along
 * the same path. Both run at the user's animation speed, so Instant shows it at once.
 */
@Composable
internal fun ExpandingAvatar(
    avatar: ChipAvatar,
    size: Dp,
    modifier: Modifier = Modifier,
) {
    val image = rememberAvatarImage(avatar.imageBytes)
    if (image == null) {
        PersonAvatar(avatar, size, modifier)
        return
    }
    // On screen, since the popup's window can start below the status bar while the page's does not.
    var origin by remember { mutableStateOf(Rect.Zero) }
    var open by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    val opening = motionTween<Float>(MotionSpeed.Relaxed, MotionCurve.Standard)
    val closing = motionTween<Float>(MotionSpeed.Relaxed, MotionCurve.Reverse)
    val scope = rememberCoroutineScope()
    LaunchedEffect(open) { if (open) progress.animateTo(1f, opening) }
    val close: () -> Unit = {
        scope.launch {
            progress.animateTo(0f, closing)
            open = false
        }
    }
    // One box holds the popup too, so a parent with gaps between children never sees it arrive.
    Box(modifier) {
        PersonAvatar(
            avatar,
            size,
            Modifier
                .onGloballyPositioned { origin = Rect(it.positionOnScreen(), it.size.toSize()) }
                // The photo above stands in for it while open, so it is never seen twice.
                .graphicsLayer { alpha = if (open) 0f else 1f }
                .clickable(remember { MutableInteractionSource() }, null, role = Role.Button) { open = true }
                .semantics { contentDescription = "Show photo" },
        )
        if (open) {
            Popup(WindowOrigin, onDismissRequest = close, properties = PopupProperties(focusable = true)) {
                ExpandedPhoto(image, origin, { progress.value }, close)
            }
        }
    }
}

@Composable
private fun ExpandedPhoto(
    image: ImageBitmap,
    origin: Rect,
    progress: () -> Float,
    onClose: () -> Unit,
) {
    // Where the popup itself sits on screen; unknown until its first layout, so the photo waits.
    var corner by remember { mutableStateOf<Offset?>(null) }
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .onGloballyPositioned { corner = it.positionOnScreen() }
            .drawBehind { drawRect(OtusColors.scrim, alpha = progress()) }
            .clickable(remember { MutableInteractionSource() }, null, onClickLabel = "Close photo", onClick = onClose),
    ) {
        val side = min(maxWidth - PageTokens.pageInset * 2, maxHeight * PHOTO_HEIGHT_FRACTION)
        Image(
            image,
            contentDescription = "Photo",
            contentScale = ContentScale.Crop,
            modifier =
                Modifier
                    .layout { measurable, constraints ->
                        val px = side.toPx()
                        val target =
                            Rect(
                                Offset((constraints.maxWidth - px) / 2, (constraints.maxHeight - px) / 2),
                                Size(px, px),
                            )
                        val box = lerp(origin.translate(-(corner ?: Offset.Zero)), target, progress())
                        val placeable =
                            measurable.measure(
                                Constraints.fixed(box.width.roundToInt(), box.height.roundToInt()),
                            )
                        layout(constraints.maxWidth, constraints.maxHeight) {
                            placeable.place(box.left.roundToInt(), box.top.roundToInt())
                        }
                    }.graphicsLayer {
                        alpha = if (corner == null) 0f else 1f
                        shape =
                            RoundedCornerShape(lerp(this.size.minDimension / 2, SheetTokens.radius.toPx(), progress()))
                        clip = true
                    },
        )
    }
}

/** Places a full-window popup at its window's corner. */
private object WindowOrigin : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset.Zero
}

private const val PHOTO_HEIGHT_FRACTION = 0.7f
