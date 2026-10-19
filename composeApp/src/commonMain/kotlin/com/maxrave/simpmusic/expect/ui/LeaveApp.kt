package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable

/**
 * Leaves the app without closing it, for the XYZ shell's Back button at the root: on Android the task
 * goes behind the launcher (playback and the screen state stay), elsewhere it does nothing.
 */
@Composable
internal expect fun rememberLeaveApp(): () -> Unit
