package com.maxrave.simpmusic.expect.ui

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
internal actual fun rememberLeaveApp(): () -> Unit {
    val activity = LocalActivity.current
    return remember(activity) {
        {
            activity?.moveTaskToBack(true)
            Unit
        }
    }
}
