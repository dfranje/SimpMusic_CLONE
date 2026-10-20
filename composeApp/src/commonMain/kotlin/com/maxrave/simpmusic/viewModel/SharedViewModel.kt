package com.maxrave.simpmusic.viewModel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.viewModelScope
import com.maxrave.common.AppIdentity
import com.maxrave.common.Config.ALBUM_CLICK
import com.maxrave.common.Config.DOWNLOAD_CACHE
import com.maxrave.common.Config.PLAYLIST_CLICK
import com.maxrave.common.Config.RECOVER_TRACK_QUEUE
import com.maxrave.common.Config.SHARE
import com.maxrave.common.Config.SONG_CLICK
import com.maxrave.common.Config.VIDEO_CLICK
import com.maxrave.common.QUALITY
import com.maxrave.common.SELECTED_LANGUAGE
import com.maxrave.domain.data.entities.AlbumEntity
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.entities.LyricsEntity
import com.maxrave.domain.data.entities.NewFormatEntity
import com.maxrave.domain.data.entities.PlaylistEntity
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.entities.SongInfoEntity
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.canvas.CanvasResult
import com.maxrave.domain.data.model.intent.GenericIntent
import com.maxrave.domain.data.model.metadata.Lyrics
import com.maxrave.domain.data.model.streams.TimeLine
import com.maxrave.domain.data.player.GenericCastState
import com.maxrave.domain.data.player.LiveStreamRegistry
import com.maxrave.domain.extension.isVideo
import com.maxrave.domain.extension.toGenericMediaItem
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.data.model.lyrics.RomanizationLanguage
import com.maxrave.domain.manager.DataStoreManager.Values.TRUE
import com.maxrave.domain.mediaservice.handler.ControlState
import com.maxrave.domain.mediaservice.handler.DownloadHandler
import com.maxrave.domain.mediaservice.handler.NowPlayingTrackState
import com.maxrave.domain.mediaservice.handler.PlayerEvent
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.domain.mediaservice.handler.SimpleMediaState
import com.maxrave.domain.mediaservice.handler.SleepTimerState
import com.maxrave.domain.mediaservice.handler.addToQueueBlock
import com.maxrave.domain.repository.AlbumRepository
import com.maxrave.domain.repository.CacheRepository
import com.maxrave.domain.repository.LocalPlaylistRepository
import com.maxrave.domain.repository.LyricsCanvasRepository
import com.maxrave.domain.repository.PlaylistRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.repository.StreamRepository
import com.maxrave.domain.utils.Resource
import com.maxrave.domain.utils.YouTubeLikeStatus
import com.maxrave.domain.utils.isEndlessQueueId
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.domain.utils.toTrack
import com.maxrave.logger.LogLevel
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.expect.isOnUnmeteredNetwork
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.theme.XYZ_ACCENT_KEY
import com.maxrave.simpmusic.ui.theme.XYZ_LIGHT_DEFAULTS_KEY
import com.maxrave.simpmusic.utils.VersionManager
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import com.maxrave.simpmusic.viewModel.base.UserPlaybackStarts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.lastOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import org.koin.core.component.inject
import org.simpmusic.lastfm.completeLogin
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.added_to_queue
import simpmusic.composeapp.generated.resources.added_to_youtube_liked
import simpmusic.composeapp.generated.resources.disliked
import simpmusic.composeapp.generated.resources.error
import simpmusic.composeapp.generated.resources.lastfm_login_failed
import simpmusic.composeapp.generated.resources.login_success
import simpmusic.composeapp.generated.resources.play_next
import simpmusic.composeapp.generated.resources.removed_from_youtube_liked
import simpmusic.composeapp.generated.resources.shared
import simpmusic.composeapp.generated.resources.updated
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

// DataStore key for the kotlin-footguns star prompt. Cleared on every version bump,
// so the ask comes back after an update - same policy as OPEN_APP_TIME.
const val FOOTGUNS_STAR_KEY = "footguns_starred"

@OptIn(ExperimentalCoroutinesApi::class)
class SharedViewModel(
    private val dataStoreManager: DataStoreManager,
    private val streamRepository: StreamRepository,
    private val songRepository: SongRepository,
    private val albumRepository: AlbumRepository,
    private val localPlaylistRepository: LocalPlaylistRepository,
    private val playlistRepository: PlaylistRepository,
    private val lyricsCanvasRepository: LyricsCanvasRepository,
    private val cacheRepository: CacheRepository,
) : BaseViewModel() {
    private val appIdentity: AppIdentity by inject()

    private var _liked: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val liked: SharedFlow<Boolean> = _liked.asSharedFlow()

    var isServiceRunning: Boolean = false

    private var _sleepTimerState = MutableStateFlow(SleepTimerState(false, 0))
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState

    private var regionCode: String? = null
    private var language: String? = null

    private var _format: MutableStateFlow<NewFormatEntity?> = MutableStateFlow(null)
    val format: SharedFlow<NewFormatEntity?> = _format.asSharedFlow()

    /**
     * Which extractor and cipher decoder produced the current track's URLs, shown in the info sheet.
     *
     * Read alongside the format rather than stored with it: the format row is cached and reused,
     * while this describes the extraction that happened in this run of the app.
     */
    private val _extractSource: MutableStateFlow<String?> = MutableStateFlow(null)
    private var _canvas: MutableStateFlow<CanvasResult?> = MutableStateFlow(null)
    val canvas: StateFlow<CanvasResult?> = _canvas

    private var canvasJob: Job? = null

    private val _intent: MutableStateFlow<GenericIntent?> = MutableStateFlow(null)
    val intent: StateFlow<GenericIntent?> = _intent

    private val _showNotificationPermissionDialog = MutableStateFlow(false)
    val showNotificationPermissionDialog: StateFlow<Boolean> = _showNotificationPermissionDialog

    private var getFormatFlowJob: Job? = null

    var playlistId: MutableStateFlow<String?> = MutableStateFlow(null)

    private var _nowPlayingState = MutableStateFlow<NowPlayingTrackState?>(null)
    val nowPlayingState: StateFlow<NowPlayingTrackState?> = _nowPlayingState

    fun getQueueDataState() = mediaPlayerHandler.queueData

    val castState: StateFlow<GenericCastState> get() = mediaPlayerHandler.castState

    private var _controllerState =
        MutableStateFlow<ControlState>(
            ControlState(
                isPlaying = false,
                isShuffle = false,
                repeatState = RepeatState.None,
                isLiked = false,
                isNextAvailable = false,
                isPreviousAvailable = false,
                isCrossfading = false,
                volume = 1f,
            ),
        )
    val controllerState: StateFlow<ControlState> = _controllerState
    private val _getVideo: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val getVideo: StateFlow<Boolean> = _getVideo

    private var _timeline =
        MutableStateFlow<TimeLine>(
            TimeLine(
                current = -1L,
                total = -1L,
                bufferedPercent = 0,
                loading = true,
            ),
        )
    val timeline: StateFlow<TimeLine> = _timeline

    private var _nowPlayingScreenData =
        MutableStateFlow<NowPlayingScreenData>(
            NowPlayingScreenData.initial(),
        )
    val nowPlayingScreenData: StateFlow<NowPlayingScreenData> = _nowPlayingScreenData

    private var _likeStatus = MutableStateFlow(YouTubeLikeStatus.INDIFFERENT)
    val likeStatus: StateFlow<YouTubeLikeStatus> = _likeStatus
    private var likeStatusJob: Job? = null

    // The track whose dislike request is still waiting for YouTube. An id rather than a flag, so the
    // pending state belongs to that track only and the next one starts out dislikeable.
    private val _dislikingVideoId = MutableStateFlow<String?>(null)
    val openAppTime: StateFlow<Int> = dataStoreManager.openAppTime.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), 0)
    init {
        viewModelScope.launch {
            log("SharedViewModel init")
            if (dataStoreManager.appVersion.first() != VersionManager.getVersionName()) {
                dataStoreManager.resetOpenAppTime()
                dataStoreManager.putString(FOOTGUNS_STAR_KEY, "false")
                dataStoreManager.setAppVersion(
                    VersionManager.getVersionName(),
                )
            }
            dataStoreManager.openApp()
            launch { applyXyzLightDefaultsOnce() }
            launch {
                dataStoreManager.watchVideoInsteadOfPlayingAudio.collectLatest {
                    _getVideo.value = it == TRUE
                }
            }
        }

        viewModelScope.launch {
            mediaPlayerHandler.nowPlayingState
                .distinctUntilChangedBy {
                    it.songEntity?.videoId
                }.collectLatest { state ->
                    canvasJob?.cancel()
                    _nowPlayingState.value = state

                    // Seed the timeline from METADATA as soon as the track is known, instead of
                    // waiting for the player. SimpleMediaState.Ready carries a duration and only
                    // fires once the container is parsed; after a queue restore nothing plays, the
                    // position poll never starts (it runs only while isPlaying), and so nothing
                    // ever reports a length — which is why a restored queue showed no times at all.
                    // SongEntity has known the length since the song was first seen.
                    //
                    // Written on EVERY track change, not just when missing: leaving the old value
                    // in place would show the previous track's length over the new one.
                    // Order matters: metadata first because it is available immediately, then the
                    // player's own duration, and only -1 ("not known yet") when neither has one.
                    //
                    // Writing -1 whenever metadata is missing — which this did at first — makes
                    // every radio track and every first-time track flash NA:NA on the clock until
                    // the container is parsed, because those rows have no durationSeconds stored.
                    // Asking the player before giving up covers exactly that case: on a normal
                    // track change it usually already knows.
                    val metadataDurationMs = (state.songEntity?.durationSeconds ?: 0).toLong() * 1000L
                    val seededTotal =
                        metadataDurationMs.takeIf { it > 0L }
                            ?: mediaPlayerHandler.getPlayerDuration().takeIf { it > 0L }
                            ?: -1L
                    _timeline.update { it.copy(total = seededTotal) }
                    state.songEntity?.let { track ->
                        _nowPlayingScreenData.value =
                            NowPlayingScreenData(
                                nowPlayingTitle = track.title,
                                artistName =
                                    track
                                        .artistName
                                        ?.joinToString(", ") ?: "",
                                isVideo = false,
                                thumbnailURL = null,
                                canvasData = null,
                                lyricsData = null,
                                songInfoData = null,
                                playlistName =
                                    mediaPlayerHandler.queueData.value
                                        ?.data
                                        ?.playlistName ?: "",
                                isRadioQueue =
                                    mediaPlayerHandler.queueData.value
                                        ?.data
                                        ?.playlistId
                                        ?.isEndlessQueueId() == true,
                            )
                    }
                    state.mediaItem.let { now ->
                        _canvas.value = null
                        getLikeStatus(now.mediaId)
                        // No song-info lookup here (description, views, likes and dislikes: two more
                        // requests per song); nothing in the XYZ shell shows it.
                        getFormat(now.mediaId)
                        _nowPlayingScreenData.update {
                            it.copy(
                                thumbnailURL = now.metadata.artworkUri,
                                isVideo = now.isVideo(),
                            )
                        }
                    }
                    state.songEntity?.let { song ->
                        _liked.value = song.liked == true
                        _nowPlayingScreenData.update {
                            it.copy(
                                isExplicit = song.isExplicit,
                            )
                        }
                    }
                }
        }
        viewModelScope.launch {
            val job1 =
                launch {
                    mediaPlayerHandler.simpleMediaState.collect { mediaState ->
                        when (mediaState) {
                            is SimpleMediaState.Buffering -> {
                                _timeline.update {
                                    it.copy(
                                        loading = true,
                                    )
                                }
                            }

                            SimpleMediaState.Initial -> {
                                _timeline.update { it.copy(loading = true) }
                            }

                            SimpleMediaState.Ended -> {
                                // Park at the end of the track rather than at -1. The only formatter
                                // for these fields renders any negative as "NA:NA", and nothing here
                                // is guaranteed to follow: at the end of the queue the player simply
                                // stays ended, so a -1 written here stays on screen. Worse, the
                                // Progress branch below ignores negative values and the Loading
                                // branch restores `total` without touching `current`, which is how
                                // the player ends up showing a correct duration next to "NA:NA".
                                _timeline.update {
                                    it.copy(
                                        current = it.total.coerceAtLeast(0L),
                                        bufferedPercent = 0,
                                        loading = false,
                                    )
                                }
                            }

                            is SimpleMediaState.Progress -> {
                                if (mediaState.progress >= 0L && mediaState.progress != _timeline.value.current) {
                                    // A live stream never reports a length (ExoPlayer: C.TIME_UNSET), so
                                    // a missing one means "loading" for everything except a live stream.
                                    if (_timeline.value.total > 0L || _timeline.value.isLive) {
                                        _timeline.update {
                                            it.copy(
                                                total = mediaPlayerHandler.getPlayerDuration().takeIf { d -> d > 0L } ?: it.total,
                                                current = mediaState.progress,
                                                loading = false,
                                            )
                                        }
                                    } else {
                                        _timeline.update {
                                            it.copy(
                                                current = mediaState.progress,
                                                loading = true,
                                                total = mediaPlayerHandler.getPlayerDuration().takeIf { d -> d > 0L } ?: it.total,
                                            )
                                        }
                                    }
                                }
                                // When progress hasn't changed (same value polled again) or is negative,
                                // don't modify loading state. The loading flag is already managed by
                                // Buffering/Ready/Loading state events. Setting loading=true here would
                                // cause rapid flickering whenever the same value arrives twice,
                                // which it can: the handler's ticker and the adapter's position
                                // poll both run at 50ms and are not in step, so a poll is sometimes
                                // read twice. A repeat is not evidence of a stall, and the loading
                                // flag belongs to the Buffering/Ready events rather than to a
                                // guess made here.
                            }

                            is SimpleMediaState.Loading -> {
                                _timeline.update {
                                    it.copy(
                                        bufferedPercent = mediaState.bufferedPercentage,
                                        total = mediaState.duration,
                                        loading = true,
                                    )
                                }
                            }

                            is SimpleMediaState.Ready -> {
                                _timeline.update {
                                    it.copy(
                                        current = mediaPlayerHandler.getProgress(),
                                        loading = false,
                                        // The player's own duration wins, but ONLY when it has one.
                                        // ExoPlayer answers C.TIME_UNSET (a large negative, not
                                        // null) until it has parsed the container, and Ready is
                                        // also published from onIsLoadingChanged — which fires
                                        // before STATE_READY. Writing that would throw away the
                                        // metadata duration seeded on the track change above.
                                        total = mediaState.duration.takeIf { d -> d > 0L } ?: it.total,
                                    )
                                }
                            }
                        }
                    }
                }
            val controllerJob =
                launch {
                    Logger.w(tag, "ControllerJob is running")
                    mediaPlayerHandler.controlState.collectLatest {
                        Logger.w(tag, "ControlState is $it")
                        _controllerState.value = it
                        // Propagate crossfade state to timeline so UI can react
                        _timeline.update { timeline ->
                            timeline.copy(isCrossfading = it.isCrossfading)
                        }
                    }
                }
            val sleepTimerJob =
                launch {
                    mediaPlayerHandler.sleepTimerState.collectLatest {
                        _sleepTimerState.value = it
                    }
                }
            val playlistNameJob =
                launch {
                    mediaPlayerHandler.queueData.collectLatest { queue ->
                        _nowPlayingScreenData.update {
                            it.copy(
                                playlistName = queue?.data?.playlistName ?: "",
                                isRadioQueue = queue?.data?.playlistId?.isEndlessQueueId() == true,
                            )
                        }
                    }
                }
            val liveStreamJob =
                launch {
                    // A track is only known to be live once its stream has been resolved, so this
                    // follows the registry as well as the track itself.
                    combine(mediaPlayerHandler.nowPlayingState, LiveStreamRegistry.liveVideoIds) { state, liveVideoIds ->
                        state.mediaItem.mediaId in liveVideoIds
                    }.distinctUntilChanged()
                        .collect { isLive -> _timeline.update { it.copy(isLive = isLive) } }
                }
            job1.join()
            controllerJob.join()
            sleepTimerJob.join()
            playlistNameJob.join()
            liveStreamJob.join()
        }
        // Reset downloading songs & playlists to not downloaded
        checkAllDownloadingSongs()
        checkAllDownloadingPlaylists()
        checkAllDownloadingLocalPlaylists()
    }

    fun setIntent(intent: GenericIntent?) {
        _intent.value = intent
    }

    /**
     * Finishes a Last.fm login from the `wordbyword://lastfm-auth` callback.
     *
     * It lands here, and not on the login screen, because the callback arrives at the app rather
     * than at any one screen: the user left for their browser, and the screen they left from may
     * not even exist any more if the process was killed. Routing the token onwards through
     * navigation would push a second copy of the login screen on top of the one already open.
     *
     * The screen learns it succeeded by watching the stored session key, not by being told.
     */
    fun completeLastfmLogin(token: String) {
        if (token.isEmpty()) return
        viewModelScope.launch {
            val session = completeLogin(token)
            if (session != null) {
                dataStoreManager.setLastfmSession(
                    sessionKey = session.sessionKey,
                    username = session.username,
                )
                makeToast(getString(Res.string.login_success))
            } else {
                makeToast(getString(Res.string.lastfm_login_failed))
            }
        }
    }

    fun showNotificationPermissionDialog() {
        _showNotificationPermissionDialog.value = true
    }

    private fun getLikeStatus(videoId: String?) {
        // Only the latest track may write: a slower reply for the previous track would otherwise
        // land on this one.
        likeStatusJob?.cancel()
        likeStatusJob =
            viewModelScope.launch {
                if (videoId != null) {
                    _likeStatus.value = YouTubeLikeStatus.INDIFFERENT
                    songRepository.getLikeStatus(videoId).collectLatest { status ->
                        _likeStatus.value = status
                    }
                }
            }
    }

    fun getString(key: String): String? = runBlocking { dataStoreManager.getString(key).first() }

    fun putString(
        key: String,
        value: String,
    ) {
        runBlocking { dataStoreManager.putString(key, value) }
    }

    fun stopSleepTimer() {
        mediaPlayerHandler.sleepStop()
    }

    private var _downloadState: MutableStateFlow<DownloadHandler.Download?> = MutableStateFlow(null)
    var downloadState: StateFlow<DownloadHandler.Download?> = _downloadState.asStateFlow()

    fun checkIsRestoring() {
        // Off the main thread: reading the download cache's keys waits until that SimpleCache has
        // finished scanning its folder, which on a slow head unit held up the first frame.
        viewModelScope.launch(Dispatchers.IO) {
            val downloadedCacheKeys = cacheRepository.getAllCacheKeys(DOWNLOAD_CACHE)
            songRepository.getDownloadedSongs().first().let { songs ->
                songs?.forEach { song ->
                    if (!downloadedCacheKeys.contains(song.videoId)) {
                        songRepository.updateDownloadState(
                            song.videoId,
                            DownloadState.STATE_NOT_DOWNLOADED,
                        )
                    }
                }
            }
            playlistRepository.getAllDownloadedPlaylist().first().let { list ->
                for (data in list) {
                    when (data) {
                        is AlbumEntity -> {
                            val tracks = data.tracks ?: emptyList()
                            if (tracks.isEmpty() ||
                                (
                                    !downloadedCacheKeys.containsAll(
                                        tracks,
                                    )
                                )
                            ) {
                                albumRepository.updateAlbumDownloadState(
                                    data.browseId,
                                    DownloadState.STATE_NOT_DOWNLOADED,
                                )
                            }
                        }

                        is PlaylistEntity -> {
                            val tracks = data.tracks ?: emptyList()
                            if (tracks.isEmpty() ||
                                (
                                    !downloadedCacheKeys.containsAll(
                                        tracks,
                                    )
                                )
                            ) {
                                playlistRepository.updatePlaylistDownloadState(
                                    data.id,
                                    DownloadState.STATE_NOT_DOWNLOADED,
                                )
                            }
                        }

                        is LocalPlaylistEntity -> {
                            val tracks = data.tracks ?: emptyList()
                            if (tracks.isEmpty() ||
                                (
                                    !downloadedCacheKeys.containsAll(
                                        tracks,
                                    )
                                )
                            ) {
                                localPlaylistRepository.updateLocalPlaylistDownloadState(
                                    DownloadState.STATE_NOT_DOWNLOADED,
                                    data.id,
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun insertLyrics(lyrics: LyricsEntity) {
        viewModelScope.launch {
            lyricsCanvasRepository.insertLyrics(lyrics)
        }
    }

    fun loadSharedMediaItem(videoId: String) {
        viewModelScope.launch {
            val localSong = songRepository.getSongById(videoId).firstOrNull()
            if (localSong != null) {
                val track = localSong.toTrack()
                mediaPlayerHandler.setQueueData(
                    QueueData.Data(
                        listTracks = arrayListOf(track),
                        firstPlayedTrack = track,
                        playlistId = "RDAMVM$videoId",
                        playlistName = getString(Res.string.shared),
                        playlistType = PlaylistType.RADIO,
                        continuation = null,
                    ),
                )
                loadMediaItemFromTrack(track, SONG_CLICK)
            } else {
                streamRepository.getFullMetadata(videoId).collectLatest { response ->
                    val track = response.data
                    when (response) {
                        is Resource.Success if (track != null) -> {
                            mediaPlayerHandler.setQueueData(
                                QueueData.Data(
                                    listTracks = arrayListOf(track),
                                    firstPlayedTrack = track,
                                    playlistId = "RDAMVM$videoId",
                                    playlistName = getString(Res.string.shared),
                                    playlistType = PlaylistType.RADIO,
                                    continuation = null,
                                ),
                            )
                            loadMediaItemFromTrack(track, SONG_CLICK)
                        }

                        else -> {
                            log("Load shared media item error: ${response.message}", LogLevel.WARN)
                            makeToast("${getString(Res.string.error)}: ${response.message}")
                        }
                    }
                }
            }
        }
    }

    fun loadMediaItemFromTrack(
        track: Track,
        type: String,
        index: Int? = null,
    ) {
        UserPlaybackStarts.notifyStarted()
        viewModelScope.launch {
            mediaPlayerHandler.clearMediaItems()
            songRepository.insertSong(track.toSongEntity()).lastOrNull()?.let {
                println("insertSong: $it")
                songRepository
                    .getSongById(track.videoId)
                    .collect { songEntity ->
                        if (songEntity != null) {
                            Logger.w("Check like", "loadMediaItemFromTrack ${songEntity.liked}")
                            _liked.value = songEntity.liked
                        }
                    }
            }
            track.durationSeconds?.let {
                songRepository.updateDurationSeconds(
                    it,
                    track.videoId,
                )
            }
            withContext(Dispatchers.Main) {
                mediaPlayerHandler.addMediaItem(track.toGenericMediaItem(), playWhenReady = type != RECOVER_TRACK_QUEUE)
            }

            when (type) {
                SONG_CLICK -> {
                    mediaPlayerHandler.getRelated(track.videoId)
                }

                VIDEO_CLICK -> {
                    mediaPlayerHandler.getRelated(track.videoId)
                }

                SHARE -> {
                    mediaPlayerHandler.getRelated(track.videoId)
                }

                PLAYLIST_CLICK -> {
                    if (index == null) {
//                                        fetchSourceFromQueue(downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = 0)
                    } else {
//                                        fetchSourceFromQueue(index!!, downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = index)
                    }
                }

                ALBUM_CLICK -> {
                    if (index == null) {
//                                        fetchSourceFromQueue(downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = 0)
                    } else {
//                                        fetchSourceFromQueue(index!!, downloaded = downloaded ?: 0)
                        loadPlaylistOrAlbum(index = index)
                    }
                }
            }
        }
    }

    fun onUIEvent(uiEvent: UIEvent) =
        viewModelScope.launch {
            when (uiEvent) {
                UIEvent.Backward -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.Backward,
                    )
                }

                UIEvent.Forward -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Forward)
                }

                UIEvent.PlayPause -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.PlayPause,
                    )
                }

                UIEvent.Next -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Next)
                }

                UIEvent.Previous -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.Previous,
                    )
                }

                UIEvent.SkipToPrevious -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.SkipToPrevious,
                    )
                }

                UIEvent.Stop -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Stop)
                }

                is UIEvent.UpdateProgress -> {
                    mediaPlayerHandler.onPlayerEvent(
                        PlayerEvent.UpdateProgress(
                            uiEvent.newProgress,
                        ),
                    )
                }

                UIEvent.Repeat -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Repeat)
                }

                UIEvent.Shuffle -> {
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.Shuffle)
                }

                UIEvent.ToggleLike -> {
                    Logger.w(tag, "ToggleLike")
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.ToggleLike)
                }

                is UIEvent.UpdateVolume -> {
                    val newVolume = uiEvent.newVolume
                    // Apply to the player first: persisting to DataStore is a suspending disk write
                    // and must not sit between the user's gesture and the audible change.
                    mediaPlayerHandler.onPlayerEvent(PlayerEvent.UpdateVolume(newVolume))
                    dataStoreManager.setPlayerVolume(newVolume)
                }
            }
        }

    override fun onCleared() {
        Logger.w("Check onCleared", "onCleared")
    }

    fun getLocation() {
        regionCode = runBlocking { dataStoreManager.location.first() }
        language = runBlocking { dataStoreManager.getString(SELECTED_LANGUAGE).first() }
    }

    private fun checkAllDownloadingLocalPlaylists() {
        viewModelScope.launch {
            localPlaylistRepository.getAllDownloadingLocalPlaylists().collectLatest { playlists ->
                playlists.forEach { playlist ->
                    localPlaylistRepository.updateDownloadState(playlist.id, 0, successMessage = getString(Res.string.updated)).lastOrNull()
                }
            }
        }
    }

    private fun checkAllDownloadingPlaylists() {
        viewModelScope.launch {
            playlistRepository.getAllDownloadingPlaylist().collectLatest { list ->
                list.forEach { data ->
                    when (data) {
                        is AlbumEntity -> {
                            albumRepository.updateAlbumDownloadState(data.browseId, 0)
                        }

                        is PlaylistEntity -> {
                            playlistRepository.updatePlaylistDownloadState(data.id, 0)
                        }

                        else -> {
                            // Skip
                        }
                    }
                }
            }
        }
    }

    private fun checkAllDownloadingSongs() {
        viewModelScope.launch {
            songRepository.getDownloadingSongs().collect { songs ->
                songs?.forEach { song ->
                    songRepository.updateDownloadState(
                        song.videoId,
                        DownloadState.STATE_NOT_DOWNLOADED,
                    )
                }
            }
            songRepository.getPreparingSongs().collect { songs ->
                songs.forEach { song ->
                    songRepository.updateDownloadState(
                        song.videoId,
                        DownloadState.STATE_NOT_DOWNLOADED,
                    )
                }
            }
        }
    }

    private var smartDownloadsJob: Job? = null

    /**
     * Smart downloads, asked for by MainActivity.onStart: every time the app comes onto the screen,
     * the only moment Android lets it start the download service. Not from init: the Glance widgets
     * create this ViewModel with the app in the background. Opening the app many times a day stays
     * cheap: three settings are read, the pass itself runs at most once a day (the run is stamped
     * before it starts), and never twice at once.
     */
    fun runSmartDownloadsIfDue() {
        if (getPlatform() != Platform.Android) return
        if (smartDownloadsJob?.isActive == true) return
        smartDownloadsJob =
            viewModelScope.launch {
                if (dataStoreManager.smartDownloadsEnabled.first() != TRUE) return@launch
                if (dataStoreManager.localTrackingEnabled.first() != TRUE) return@launch
                val nowMillis = Clock.System.now().toEpochMilliseconds()
                val sinceLastRun = nowMillis - dataStoreManager.smartDownloadsLastRun.first()
                // A last run stamped in the future (the clock was moved back) does not block the pass.
                if (sinceLastRun in 0 until 1.days.inWholeMilliseconds) return@launch
                if (!isOnUnmeteredNetwork()) return@launch
                // Stamped first: a pass that fails is not retried at every launch, and a call arriving
                // while this one runs finds the day already done.
                dataStoreManager.setSmartDownloadsLastRun(nowMillis)
                songRepository.runSmartDownloads(dataStoreManager.smartDownloadsCount.first())
            }
    }

    private fun getFormat(mediaId: String?) {
        if (mediaId != _format.value?.videoId && !mediaId.isNullOrEmpty()) {
            _format.value = null
            _extractSource.value = streamRepository.getExtractSource(mediaId)
            getFormatFlowJob?.cancel()
            getFormatFlowJob =
                viewModelScope.launch {
                    streamRepository.getFormatFlow(mediaId).cancellable().collectLatest { f ->
                        Logger.d(tag, "Get format for $mediaId: itag ${f?.itag}, expires ${f?.expiredTime}")
                        if (f != null) {
                            _format.emit(f)
                        } else {
                            _format.emit(null)
                        }
                        // Re-read on every emission: the first one usually lands before the
                        // extractor has finished, so the source is only known on a later pass.
                        _extractSource.value = streamRepository.getExtractSource(mediaId)
                    }
                }
        }
    }

    private var songInfoJob: Job? = null

    fun getSongInfo(mediaId: String?) {
        songInfoJob?.cancel()
        songInfoJob =
            viewModelScope.launch {
                if (mediaId != null) {
                    songRepository.getSongInfo(mediaId).collect { song ->
                        _nowPlayingScreenData.update {
                            it.copy(
                                songInfoData = song,
                            )
                        }
                    }
                }
            }
    }

    private fun loadPlaylistOrAlbum(index: Int? = null) {
        mediaPlayerHandler.loadPlaylistOrAlbum(index)
    }

    private var _recreateActivity: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val recreateActivity: StateFlow<Boolean> = _recreateActivity

    fun activityRecreate() {
        _recreateActivity.value = true
    }

    fun activityRecreateDone() {
        _recreateActivity.value = false
    }

    fun addListToQueue(listTrack: ArrayList<Track>) {
        viewModelScope.launch {
            // A radio, a mix or an Endless queue has no end to append to, so a single song (the swipe
            // on a song row) goes right after the current one instead.
            val endlessQueue = dataStoreManager.endlessQueue.first() == TRUE
            if (listTrack.size == 1 && mediaPlayerHandler.queueData.value.addToQueueBlock(endlessQueue) != null) {
                mediaPlayerHandler.playNext(listTrack.first())
                makeToast(getString(Res.string.play_next))
            } else {
                mediaPlayerHandler.loadMoreCatalog(listTrack)
                makeToast(getString(Res.string.added_to_queue))
            }
        }
    }

    fun addToYouTubeLiked() {
        viewModelScope.launch {
            val videoId = mediaPlayerHandler.nowPlaying.first()?.mediaId
            if (videoId != null) {
                val like = likeStatus.value == YouTubeLikeStatus.LIKE
                if (!like) {
                    songRepository
                        .addToYouTubeLiked(
                            mediaPlayerHandler.nowPlaying.first()?.mediaId,
                        ).collect { response ->
                            if (response == 200) {
                                makeToast(getString(Res.string.added_to_youtube_liked))
                                getLikeStatus(videoId)
                            } else {
                                makeToast(getString(Res.string.error))
                            }
                        }
                } else {
                    songRepository
                        .removeFromYouTubeLiked(
                            mediaPlayerHandler.nowPlaying.first()?.mediaId,
                        ).collect {
                            if (it == 200) {
                                makeToast(getString(Res.string.removed_from_youtube_liked))
                                getLikeStatus(videoId)
                            } else {
                                makeToast(getString(Res.string.error))
                            }
                        }
                }
            }
        }
    }

    /**
     * Dislikes [videoId], the track the sheet was opened for. While the request is out the action
     * reads "Disliking…" and cannot be pressed again; a failure puts it back as it was.
     */
    fun addToYouTubeDisliked(videoId: String) {
        if (videoId.isEmpty() || _dislikingVideoId.value == videoId) return
        _dislikingVideoId.value = videoId
        viewModelScope.launch {
            try {
                songRepository.addToYouTubeDisliked(videoId).collect { response ->
                    if (response == 200) {
                        // The reply can arrive after the player moved on; it belongs to the
                        // track it was sent for, so it only shows if that track is still playing.
                        if (mediaPlayerHandler.nowPlaying.value?.mediaId == videoId) {
                            _likeStatus.value = YouTubeLikeStatus.DISLIKE
                        }
                        makeToast(getString(Res.string.disliked))
                    } else {
                        makeToast(getString(Res.string.error))
                    }
                }
            } finally {
                _dislikingVideoId.compareAndSet(videoId, null)
            }
        }
    }

    fun getEnableLiquidGlass() = dataStoreManager.enableLiquidGlass

    // Drives the Mix for you tab: YouTube hands an anonymous session no mixes at all.
    fun getYouTubeLoggedIn() = dataStoreManager.loggedIn

    fun getThemeMode() = dataStoreManager.themeMode

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStoreManager.setThemeMode(mode)
        }
    }

    /** The XYZ look's accent as stored: "follow", an index into XyzAccents, or null for the default. */
    fun getXyzAccent() = dataStoreManager.getString(XYZ_ACCENT_KEY)

    fun setXyzAccent(setting: String) {
        viewModelScope.launch {
            dataStoreManager.putString(XYZ_ACCENT_KEY, setting)
        }
    }

    /**
     * The defaults of this light build, applied once to an install that predates them; a fresh install
     * already starts on them. Video, delay, reverb, liquid glass and the update check have no row in
     * Settings any more, so they are switched off for good here.
     */
    private suspend fun applyXyzLightDefaultsOnce() {
        if (dataStoreManager.getString(XYZ_LIGHT_DEFAULTS_KEY).first() != null) return
        dataStoreManager.setNormalizeVolume(true)
        dataStoreManager.setSkipSilent(true)
        dataStoreManager.setQuality(QUALITY.items[0].toString())
        dataStoreManager.setMaxSongCacheSize(500)
        dataStoreManager.setWatchVideoInsteadOfPlayingAudio(false)
        dataStoreManager.setDelayEnabled(false)
        dataStoreManager.setReverbEnabled(false)
        dataStoreManager.setEnableLiquidGlass(false)
        dataStoreManager.setAutoCheckForUpdates(false)
        dataStoreManager.setBlogNotificationEnabled(false)
        dataStoreManager.putString(XYZ_LIGHT_DEFAULTS_KEY, TRUE)
    }

    fun setNowPlayingStyle(style: String) {
        viewModelScope.launch {
            dataStoreManager.setNowPlayingStyle(style)
        }
    }

    fun setLyricsStyle(style: String) {
        viewModelScope.launch {
            dataStoreManager.setLyricsStyle(style)
        }
    }

    fun setRomanizationLanguages(languages: Set<RomanizationLanguage>) {
        viewModelScope.launch {
            // Sorted by name so the stored string is stable: an unsorted Set writes a different
            // value for the same selection depending on iteration order, which makes the DataStore
            // flow emit on a change that did not happen.
            dataStoreManager.setRomanizationLanguages(languages.map { it.name }.sorted().joinToString(","))
        }
    }

    fun shouldStopMusicService(): Boolean = runBlocking { dataStoreManager.killServiceOnExit.first() == TRUE }

}

sealed class UIEvent {
    data object PlayPause : UIEvent()

    data object Backward : UIEvent()

    data object Forward : UIEvent()

    data object Next : UIEvent()

    data object Previous : UIEvent()

    /**
     * Always advances to the previous track — bypasses the 3-second
     * "seek to start of current track" rule used by [Previous]. Used by the
     * NowPlaying artwork pager swipe.
     */
    data object SkipToPrevious : UIEvent()

    data object Stop : UIEvent()

    data object Shuffle : UIEvent()

    data object Repeat : UIEvent()

    data class UpdateProgress(
        val newProgress: Float,
    ) : UIEvent()

    data class UpdateVolume(
        val newVolume: Float,
    ) : UIEvent()

    data object ToggleLike : UIEvent()
}

enum class LyricsProvider {
    SIMPMUSIC,
    YOUTUBE,
    SPOTIFY,
    LRCLIB,
    BETTER_LYRICS,
    AI,
    OFFLINE,
}

data class NowPlayingScreenData(
    val playlistName: String,
    /** The queue plays from a radio or mix id, so the top bar says Radio/Mix instead of Now Playing. */
    val isRadioQueue: Boolean = false,
    val nowPlayingTitle: String,
    val artistName: String,
    val isVideo: Boolean,
    val isExplicit: Boolean = false,
    val thumbnailURL: String?,
    val canvasData: CanvasData? = null,
    val lyricsData: LyricsData? = null,
    val songInfoData: SongInfoEntity? = null,
    val bitmap: ImageBitmap? = null,
) {
    data class CanvasData(
        val isVideo: Boolean,
        val url: String,
        // A still of the clip at the clip's own size — Apple Music's animated artwork sends one.
        // The Apple Music player shows it under the clip while that loads, and reads the clip's
        // proportions off it, so the frame is the right shape before the first video frame arrives.
        val thumbUrl: String? = null,
    )

    data class LyricsData(
        val lyrics: Lyrics,
        val translatedLyrics: Pair<Lyrics, LyricsProvider>? = null,
        val lyricsProvider: LyricsProvider,
    )

    companion object {
        fun initial(): NowPlayingScreenData =
            NowPlayingScreenData(
                nowPlayingTitle = "",
                artistName = "",
                isVideo = false,
                thumbnailURL = null,
                canvasData = null,
                lyricsData = null,
                songInfoData = null,
                playlistName = "",
            )
    }
}
