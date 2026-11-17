package com.otusfoss.core.designsystem.mail.read.elements

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@Composable
public actual fun HtmlViewWarmUp() {
    val context = LocalContext.current.applicationContext
    LaunchedEffect(Unit) {
        if (warmed) return@LaunchedEffect
        // The screen that asked settles first, so the start drops no frame of its arrival.
        delay(SETTLE)
        warmed = true
        // The first view of a process loads and starts the engine; the view itself is not needed.
        WebView(context).destroy()
    }
}

/** Whether this process started its web engine here; read and set on the main thread only. */
private var warmed = false

private val SETTLE = 500.milliseconds
