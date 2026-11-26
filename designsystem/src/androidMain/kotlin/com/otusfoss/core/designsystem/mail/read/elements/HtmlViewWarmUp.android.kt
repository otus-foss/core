package com.otusfoss.core.designsystem.mail.read.elements

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
public actual fun HtmlViewWarmUp() {
    val context = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        if (warmed) return@LaunchedEffect
        // Claimed before the wait, so a second warm-up composed meanwhile starts nothing.
        warmed = true
        try {
            // The screen that asked settles first, so the start drops no frame of its arrival.
            delay(SETTLE)
        } catch (left: CancellationException) {
            // The screen left before the start, so the next one to ask starts it.
            warmed = false
            throw left
        }
        // The first view of a process loads and starts the engine; the view itself is not needed.
        WebView(context).destroy()
    }
}

/** Whether this process started its web engine here, or is about to; read and set on the main thread only. */
private var warmed = false

private val SETTLE = 500.milliseconds
