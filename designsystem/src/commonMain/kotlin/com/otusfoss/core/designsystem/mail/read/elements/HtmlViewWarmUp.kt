package com.otusfoss.core.designsystem.mail.read.elements

import androidx.compose.runtime.Composable

/**
 * Starts the platform's web engine ahead of the first [HtmlView], once per process, so the first
 * message a user opens does not wait for it. Compose it on a screen that is already drawn and
 * idle, such as a list after the launch animation: the start takes a moment on the main thread.
 * It loads no page and makes no network request.
 */
@Composable
public expect fun HtmlViewWarmUp()
