package com.otus.designsystem.theme.motion

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState

/**
 * Scrolls to the item at [index] on [animationSpec], such as a [motionTween] at the user's
 * animation speed, where Compose's own `animateScrollToItem` always runs its own spring. An item
 * out of view is reached by an estimate from the items in view, then placed exactly.
 */
internal suspend fun LazyListState.animateScrollToItem(
    index: Int,
    animationSpec: AnimationSpec<Float>,
) {
    val layout = layoutInfo
    val target = layout.visibleItemsInfo.firstOrNull { it.index == index }
    val distance =
        target?.offset?.toFloat() ?: run {
            val rows = layout.visibleItemsInfo.takeIf { it.isNotEmpty() } ?: return scrollToItem(index)
            val step = rows.sumOf { it.size + layout.mainAxisItemSpacing }.toFloat() / rows.size
            step * (index - firstVisibleItemIndex) - firstVisibleItemScrollOffset
        }
    animateScrollBy(distance, animationSpec)
    if (target == null) scrollToItem(index)
}
