package com.maxrave.simpmusic.viewModel

import androidx.lifecycle.viewModelScope
import com.maxrave.common.Config
import com.maxrave.common.Config.REMOVED_SONG_DATE_TIME
import com.maxrave.domain.data.entities.ArtistEntity
import com.maxrave.domain.data.entities.DownloadState
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.repository.ArtistRepository
import com.maxrave.domain.repository.CacheRepository
import com.maxrave.domain.repository.SongRepository
import com.maxrave.domain.utils.toArrayListTrack
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.playlist

class LibraryDynamicPlaylistViewModel(
    private val songRepository: SongRepository,
    private val artistRepository: ArtistRepository,
    private val cacheRepository: CacheRepository,
) : BaseViewModel() {
    private val _listFavoriteSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listFavoriteSong: StateFlow<List<SongEntity>> get() = _listFavoriteSong

    private val _listFollowedArtist: MutableStateFlow<List<ArtistEntity>> = MutableStateFlow(emptyList())
    val listFollowedArtist: StateFlow<List<ArtistEntity>> get() = _listFollowedArtist

    private val _listMostPlayedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listMostPlayedSong: StateFlow<List<SongEntity>> get() = _listMostPlayedSong

    private val _listDownloadedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listDownloadedSong: StateFlow<List<SongEntity>> get() = _listDownloadedSong

    /**
     * Liked songs crediting one artist, filled in once a route names the artist, since there is no
     * artist to observe until then.
     */
    private val _listArtistLikedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listArtistLikedSong: StateFlow<List<SongEntity>> get() = _listArtistLikedSong

    /** That artist's name for the header, or null when the artist was never stored. */
    private val _artistLikedName: MutableStateFlow<String?> = MutableStateFlow(null)
    val artistLikedName: StateFlow<String?> get() = _artistLikedName

    /**
     * Songs the player cache holds in full and that are not downloaded — what plays offline without
     * having been downloaded. Loaded on demand: the player cache is no database
     * table, so there is nothing to observe, and the screen reloads it each time it opens.
     */
    private val _listCachedSong: MutableStateFlow<List<SongEntity>> = MutableStateFlow(emptyList())
    val listCachedSong: StateFlow<List<SongEntity>> get() = _listCachedSong
    private var cachedSongJob: Job? = null

    init {
        getFavoriteSong()
        getFollowedArtist()
        getMostPlayedSong()
        getDownloadedSong()
    }

    private fun getFavoriteSong() {
        viewModelScope.launch {
            songRepository.getLikedSongs().collectLatest { likedSong ->
                _listFavoriteSong.value =
                    likedSong.sortedByDescending {
                        it.favoriteAt ?: REMOVED_SONG_DATE_TIME
                    }
            }
        }
    }

    private fun getFollowedArtist() {
        viewModelScope.launch {
            artistRepository.getFollowedArtists().collectLatest { followedArtist ->
                _listFollowedArtist.value =
                    followedArtist.sortedByDescending {
                        it.followedAt ?: REMOVED_SONG_DATE_TIME
                    }
            }
        }
    }

    private fun getMostPlayedSong() {
        viewModelScope.launch {
            songRepository.getMostPlayedSongs().collectLatest { mostPlayedSong ->
                _listMostPlayedSong.value = mostPlayedSong.sortedByDescending { it.totalPlayTime }
            }
        }
    }

    private fun getDownloadedSong() {
        viewModelScope.launch {
            songRepository.getDownloadedSongs().collectLatest { downloadedSong ->
                _listDownloadedSong.value =
                    (downloadedSong ?: emptyList()).sortedByDescending {
                        it.downloadedAt ?: REMOVED_SONG_DATE_TIME
                    }
            }
        }
    }

    /**
     * Liked songs crediting [artistLiked]'s artist, ordered like [getFavoriteSong] so this list and
     * Favorites agree. Observed rather than read once: unliking a song here drops it from the list.
     */
    fun getArtistLikedSong(artistLiked: LibraryDynamicPlaylistType.ArtistLiked) {
        viewModelScope.launch {
            _artistLikedName.value = artistRepository.getArtistById(artistLiked.channelId).firstOrNull()?.name
        }
        viewModelScope.launch {
            songRepository.getLikedSongsByArtist(artistLiked.channelId).collectLatest { likedSong ->
                _listArtistLikedSong.value =
                    likedSong.sortedByDescending {
                        it.favoriteAt ?: REMOVED_SONG_DATE_TIME
                    }
            }
        }
    }

    fun getCachedSong() {
        cachedSongJob?.cancel()
        cachedSongJob =
            viewModelScope.launch {
                _listCachedSong.value = loadCachedSongs(cacheRepository, songRepository)
            }
    }

    fun removeFromCache(videoId: String) {
        viewModelScope.launch {
            cacheRepository.removeCachedSong(videoId)
            getCachedSong()
        }
    }

    /** What the player calls the queue this screen started. */
    private fun playlistName(type: LibraryDynamicPlaylistType): String {
        val name =
            when (type) {
                is LibraryDynamicPlaylistType.ArtistLiked ->
                    listOfNotNull(getString(type.name()), _artistLikedName.value).joinToString(" · ")
                else -> getString(type.name())
            }
        return "${getString(Res.string.playlist)} $name"
    }

    fun playSong(
        videoId: String,
        type: LibraryDynamicPlaylistType,
    ) {
        val (targetList, playTrack) =
            when (type) {
                LibraryDynamicPlaylistType.Favorite -> listFavoriteSong.value to listFavoriteSong.value.find { it.videoId == videoId }
                LibraryDynamicPlaylistType.Downloaded -> listDownloadedSong.value to listDownloadedSong.value.find { it.videoId == videoId }
                LibraryDynamicPlaylistType.Cached -> listCachedSong.value to listCachedSong.value.find { it.videoId == videoId }
                LibraryDynamicPlaylistType.Followed -> return
                LibraryDynamicPlaylistType.MostPlayed -> listMostPlayedSong.value to listMostPlayedSong.value.find { it.videoId == videoId }
                is LibraryDynamicPlaylistType.ArtistLiked ->
                    listArtistLikedSong.value to listArtistLikedSong.value.find { it.videoId == videoId }
                else -> return
            }
        if (playTrack == null) return
        setQueueData(
            QueueData.Data(
                listTracks = targetList.toArrayListTrack(),
                firstPlayedTrack = playTrack.toTrack(),
                playlistId = null,
                playlistName = playlistName(type),
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            playTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            targetList.indexOf(playTrack).coerceAtLeast(0),
        )
    }

    private fun getSongList(type: LibraryDynamicPlaylistType): List<SongEntity> =
        when (type) {
            LibraryDynamicPlaylistType.Favorite -> listFavoriteSong.value
            LibraryDynamicPlaylistType.Downloaded -> listDownloadedSong.value
            LibraryDynamicPlaylistType.Cached -> listCachedSong.value
            LibraryDynamicPlaylistType.MostPlayed -> listMostPlayedSong.value
            is LibraryDynamicPlaylistType.ArtistLiked -> listArtistLikedSong.value
            else -> emptyList()
        }

    fun playAll(type: LibraryDynamicPlaylistType) {
        val targetList = getSongList(type)
        val firstTrack = targetList.firstOrNull() ?: return
        setQueueData(
            QueueData.Data(
                listTracks = targetList.toArrayListTrack(),
                firstPlayedTrack = firstTrack.toTrack(),
                playlistId = null,
                playlistName = playlistName(type),
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            firstTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            0,
        )
    }

    fun shuffle(type: LibraryDynamicPlaylistType) {
        val targetList = getSongList(type)
        if (targetList.isEmpty()) return
        val shuffledList = targetList.shuffled()
        val firstTrack = shuffledList.first()
        setQueueData(
            QueueData.Data(
                listTracks = shuffledList.toArrayListTrack(),
                firstPlayedTrack = firstTrack.toTrack(),
                playlistId = null,
                playlistName = playlistName(type),
                playlistType = PlaylistType.RADIO,
                continuation = null,
            ),
        )
        loadMediaItem(
            firstTrack.toTrack(),
            Config.PLAYLIST_CLICK,
            0,
        )
    }
}

/**
 * Songs the player cache holds in full and that are not downloaded, last played first — the
 * "Cached" list, and the card in Library that previews it. A cached song whose row was swept by
 * "clear listening history" has nothing to show, so it is left out.
 */
internal suspend fun loadCachedSongs(
    cacheRepository: CacheRepository,
    songRepository: SongRepository,
): List<SongEntity> {
    val ids = cacheRepository.getFullyCachedSongIds()
    if (ids.isEmpty()) return emptyList()
    return songRepository
        .getSongsByListVideoId(ids)
        .first()
        .filter { it.downloadState != DownloadState.STATE_DOWNLOADED }
        .sortedByDescending { it.inLibrary }
}