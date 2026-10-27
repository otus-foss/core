package com.otus.designsystem.shared.pages.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.Measurable
import androidx.compose.ui.layout.MeasureResult
import androidx.compose.ui.layout.MeasureScope
import androidx.compose.ui.node.CompositionLocalConsumerModifierNode
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.currentValueOf
import androidx.compose.ui.node.requireGraphicsContext
import androidx.compose.ui.node.requireLayoutCoordinates
import androidx.compose.ui.unit.Constraints

/**
 * Keeps this [kind] of chrome still while its page slides against another page that has it too.
 * Meanwhile the outermost slider draws it above every page, so sliding content passes under it.
 */
internal fun Modifier.pinnedChrome(kind: PageChrome): Modifier = this then PinnedChromeElement(kind)

/** The chrome of every page in a slider, which the slider draws over its pages while it stays still. */
internal class PinnedChromeHost {
    val chrome = mutableStateListOf<PinnedChromeNode>()
}

/** [parent]'s host, or a new one for the outermost slider. */
@Composable
internal fun rememberChromeHost(parent: PageMotion?): PinnedChromeHost {
    val own = remember { PinnedChromeHost() }
    return parent?.host ?: own
}

/** The outermost slider, without a [parent], draws the still chrome of every page inside it. */
internal fun Modifier.hostingChrome(
    parent: PageMotion?,
    host: PinnedChromeHost,
): Modifier = if (parent == null) this then PinnedChromeHostElement(host) else this

private data class PinnedChromeElement(
    val kind: PageChrome,
) : ModifierNodeElement<PinnedChromeNode>() {
    override fun create() = PinnedChromeNode(kind)

    override fun update(node: PinnedChromeNode) {
        node.update(kind)
    }
}

internal class PinnedChromeNode(
    private var kind: PageChrome,
) : Modifier.Node(),
    LayoutModifierNode,
    DrawModifierNode,
    CompositionLocalConsumerModifierNode {
    private var motion: PageMotion? = null
    private var layer: GraphicsLayer? = null

    private val pinned: Boolean
        get() = motion?.pinned(kind) == true

    val leaves: Boolean
        get() = motion?.leaves(kind) == true

    override fun onAttach() {
        layer = requireGraphicsContext().createGraphicsLayer()
        motion =
            currentValueOf(LocalPageMotion)?.also {
                it.register(kind)
                it.host.chrome += this
            }
    }

    override fun onDetach() {
        motion?.let {
            it.unregister(kind)
            it.host.chrome -= this
        }
        motion = null
        layer?.let { requireGraphicsContext().releaseGraphicsLayer(it) }
        layer = null
    }

    fun update(kind: PageChrome) {
        motion?.unregister(this.kind)
        this.kind = kind
        motion?.register(kind)
    }

    override fun MeasureScope.measure(
        measurable: Measurable,
        constraints: Constraints,
    ): MeasureResult {
        val placeable = measurable.measure(constraints)
        return layout(placeable.width, placeable.height) {
            // Undoing the slide here also keeps touches on the chrome where it is drawn.
            placeable.placeWithLayer(0, 0) { translationX = motion?.pinnedShift(kind) ?: 0f }
        }
    }

    /** While pinned, records the chrome for the host to draw instead of drawing it inside the page. */
    override fun ContentDrawScope.draw() {
        val layer = layer
        if (pinned && layer != null) layer.record { this@draw.drawContent() } else drawContent()
    }

    fun DrawScope.drawPinned(host: LayoutCoordinates) {
        val layer = layer
        val motion = motion
        if (layer == null || motion == null || !pinned) return
        layer.alpha = motion.pinnedAlpha(kind)
        // This node sits outside the layer that undoes the slide, so its position still slides.
        val at = host.localPositionOf(requireLayoutCoordinates(), Offset.Zero)
        translate(at.x + motion.pinnedShift(kind), at.y) { drawLayer(layer) }
    }
}

private data class PinnedChromeHostElement(
    val host: PinnedChromeHost,
) : ModifierNodeElement<PinnedChromeHostNode>() {
    override fun create() = PinnedChromeHostNode(host)

    override fun update(node: PinnedChromeHostNode) {
        node.host = host
    }
}

/** Draws the host's still chrome over the slider's pages: the leaving page's copies first, then the arriving page's. */
private class PinnedChromeHostNode(
    var host: PinnedChromeHost,
) : Modifier.Node(),
    DrawModifierNode {
    override fun ContentDrawScope.draw() {
        drawContent()
        val origin = requireLayoutCoordinates()
        host.chrome.sortedByDescending { it.leaves }.forEach { with(it) { drawPinned(origin) } }
    }
}
