package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable
import com.maxrave.simpmusic.ui.theme.XyzPalette

// The launcher is an Android home screen; on Desktop the XYZ look runs on the accent picked in Settings.
actual fun canFollowXyzLauncher(): Boolean = false

@Composable
actual fun rememberXyzLauncherPalette(enabled: Boolean): XyzPalette? = null
