package com.maxrave.simpmusic.viewModel.base

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Every time the user starts something playing: a song, playlist, album or radio picked anywhere in the
 * app, or a shared link. Raised from the two places all of those go through
 * ([BaseViewModel.loadMediaItem] and SharedViewModel.loadMediaItemFromTrack); the player's own
 * transport and the queue rows on Home do not go through either. The XYZ shell (App.kt) opens Home on
 * each, so picking a song lands on the player.
 */
object UserPlaybackStarts {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun notifyStarted() {
        _events.tryEmit(Unit)
    }
}
