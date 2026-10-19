package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable
import com.maxrave.simpmusic.ui.theme.XyzPalette

/** Whether this platform can read the XYZ Launcher's palette (Android, where the launcher runs). */
expect fun canFollowXyzLauncher(): Boolean

/**
 * The XYZ Launcher's palette as it is drawn right now, kept live while [enabled]: re-read whenever the
 * launcher reports a change and whenever the app comes back to the front. Null while disabled, before
 * the first read, and wherever the launcher is not installed.
 */
@Composable
expect fun rememberXyzLauncherPalette(enabled: Boolean): XyzPalette?
