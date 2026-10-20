package com.maxrave.data.mediaservice

import com.maxrave.domain.data.entities.SongEntity

/**
 * Stand-in for kizzy's DiscordRPC: the XYZ build ships no Discord Rich Presence, and no kizzy. It never
 * connects, so the handlers' presence plumbing stays as it was and simply has nothing to talk to.
 */
internal class DiscordRPC(
    @Suppress("UNUSED_PARAMETER") token: String,
) {
    fun isRpcRunning(): Boolean = false

    fun closeRPC() = Unit

    @Suppress("UNUSED_PARAMETER")
    suspend fun updateSong(
        currentPlaybackTimeMillis: Long,
        durationMillis: Long,
        playbackSpeed: Float = 1.0f,
        song: SongEntity,
    ): Result<Unit> = Result.success(Unit)
}
