package com.otus.designsystem.mail.quickview.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import com.otus.designsystem.mail.attachments.components.AttachmentKind
import com.otus.designsystem.shared.controls.elements.CountBadge
import com.otus.designsystem.shared.controls.elements.RollingWords
import com.otus.designsystem.shared.controls.elements.Symbol
import com.otus.designsystem.shared.panels.components.scrollFade
import com.otus.designsystem.shared.people.components.ChipAvatar
import com.otus.designsystem.shared.people.components.PersonAvatar
import com.otus.designsystem.theme.colors.OtusColors
import com.otus.designsystem.theme.tokens.layout.CornerTokens
import com.otus.designsystem.theme.tokens.layout.QuickViewTokens
import kotlin.math.ceil

/** What a [QuickFace] shows in its circle. */
public sealed interface QuickFaceLook {
    /** A person's or an address's avatar. */
    public class Person(
        public val avatar: ChipAvatar,
    ) : QuickFaceLook

    /** A file, on the round tile its [kind] gets. */
    public class File(
        public val kind: AttachmentKind,
    ) : QuickFaceLook
}

/**
 * One circle in [QuickFaces] with its [label] under it, read out as [description]. A [count] above
 * 0 shows in an accent badge on the circle's corner; [unread] shows a dot there instead.
 */
public class QuickFace(
    public val label: String,
    public val description: String,
    public val look: QuickFaceLook,
    public val onClick: () -> Unit,
    public val count: Int = 0,
    public val unread: Boolean = false,
)

/**
 * A quick view module of up to five [faces] in a row, such as the people the user writes to most,
 * their addresses, or recent files. Fewer faces keep their width and start at the left.
 */
@Composable
public fun QuickFaces(
    faces: List<QuickFace>,
    modifier: Modifier = Modifier,
    slots: Int = faces.size.coerceAtLeast(1),
) {
    QuickModule(modifier, Arrangement.Start) {
        faces.take(slots).forEach { face -> Face(face, Modifier.weight(1f)) }
        repeat(slots - faces.size.coerceAtMost(slots)) { Box(Modifier.weight(1f)) }
    }
}

/**
 * [faces] in a row that scrolls sideways, such as the people a search finds, fading at an edge
 * with more faces past it. A "+N" pill at the row's end counts the faces still to the right.
 */
@Composable
public fun ScrollingFaces(
    faces: List<QuickFace>,
    modifier: Modifier = Modifier,
) {
    val scroll = rememberScrollState()
    val slot = with(LocalDensity.current) { QuickViewTokens.faceSlot.toPx() }
    // Faces wholly or partly past the right edge.
    val left by remember(scroll, slot) { derivedStateOf { ceil((scroll.maxValue - scroll.value) / slot).toInt() } }
    Box(modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().scrollFade(scroll).horizontalScroll(scroll, overscrollEffect = null)) {
            faces.forEach { face -> Face(face, Modifier.width(QuickViewTokens.faceSlot)) }
        }
        if (left > 0) MorePill(left, Modifier.align(Alignment.TopEnd))
    }
}

/** How many more faces wait to the right, centered on the faces' circles. */
@Composable
private fun MorePill(
    count: Int,
    modifier: Modifier,
) {
    Box(
        modifier
            .height(QuickViewTokens.faceSize)
            .wrapContentHeight(),
    ) {
        // The count rolls as the row scrolls, as the Show button's does when filters change.
        RollingWords(
            "+$count",
            Modifier
                .background(OtusColors.bgLight, CircleShape)
                .padding(horizontal = QuickViewTokens.morePadding, vertical = QuickViewTokens.dotRing),
            TextStyle(
                color = OtusColors.text1,
                fontSize = QuickViewTokens.labelSize,
                lineHeight = QuickViewTokens.labelLineHeight,
            ),
        )
    }
}

@Composable
private fun Face(
    face: QuickFace,
    modifier: Modifier,
) {
    Column(
        modifier.clip(FaceShape).quickClick(face.description, FaceShape, face.onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(QuickViewTokens.tileLabelGap),
    ) {
        Box {
            when (val look = face.look) {
                is QuickFaceLook.Person -> {
                    PersonAvatar(look.avatar, QuickViewTokens.faceSize)
                }

                is QuickFaceLook.File -> {
                    Box(
                        Modifier.size(QuickViewTokens.faceSize).background(OtusColors.bgLight, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Symbol(look.kind.icon) }
                }
            }
            FaceMark(face, Modifier.align(Alignment.TopEnd))
        }
        QuickLabel(face.label, if (face.unread || face.count > 0) OtusColors.text1 else OtusColors.text2)
    }
}

@Composable
private fun FaceMark(
    face: QuickFace,
    modifier: Modifier,
) {
    when {
        face.count > 0 -> {
            CountBadge(
                face.count.toString(),
                modifier
                    .offset(x = QuickViewTokens.dotRing * 2)
                    .border(QuickViewTokens.dotRing, OtusColors.bgDark, CircleShape),
            )
        }

        face.unread -> {
            Box(
                modifier
                    .size(QuickViewTokens.dotSize + QuickViewTokens.dotRing * 2)
                    .background(OtusColors.bgDark, CircleShape)
                    .padding(QuickViewTokens.dotRing)
                    .background(OtusColors.accentRegular, CircleShape),
            )
        }
    }
}

private val FaceShape = RoundedCornerShape(CornerTokens.chip)
