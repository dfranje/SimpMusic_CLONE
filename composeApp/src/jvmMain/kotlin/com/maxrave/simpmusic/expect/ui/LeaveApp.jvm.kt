package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable

// A desktop window has nowhere behind it to go back to.
@Composable
internal actual fun rememberLeaveApp(): () -> Unit = {}
