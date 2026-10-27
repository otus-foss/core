package com.otus.designsystem.shared.overlays.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.rememberNavigationEventDispatcherOwner

/** Gives [content] a root back dispatcher, which the test activity does not provide. */
@Composable
internal fun WithBackDispatcher(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalNavigationEventDispatcherOwner provides rememberNavigationEventDispatcherOwner(parent = null),
        content = content,
    )
}
