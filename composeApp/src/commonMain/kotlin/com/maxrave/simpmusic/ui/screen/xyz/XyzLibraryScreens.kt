package com.maxrave.simpmusic.ui.screen.xyz

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.maxrave.common.Config
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.searchResult.songs.SongsResult
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.repository.ArtistRepository
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.ui.component.ArtistFullWidthItems
import com.maxrave.simpmusic.ui.component.SongFullWidthItems
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.LibraryDynamicPlaylistViewModel
import com.maxrave.simpmusic.viewModel.SearchScreenUIState
import com.maxrave.simpmusic.viewModel.SearchViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.artists
import simpmusic.composeapp.generated.resources.liked_songs
import simpmusic.composeapp.generated.resources.liked_songs_count
import simpmusic.composeapp.generated.resources.play_all
import simpmusic.composeapp.generated.resources.shuffle
import simpmusic.composeapp.generated.resources.songs
import simpmusic.composeapp.generated.resources.xyz_empty_artists
import simpmusic.composeapp.generated.resources.xyz_empty_favorite_artists
import simpmusic.composeapp.generated.resources.xyz_empty_favorite_songs
import simpmusic.composeapp.generated.resources.xyz_empty_songs
import simpmusic.composeapp.generated.resources.xyz_favorites
import simpmusic.composeapp.generated.resources.xyz_most_played_artists
import simpmusic.composeapp.generated.resources.xyz_most_played_songs
import simpmusic.composeapp.generated.resources.xyz_no_results
import simpmusic.composeapp.generated.resources.xyz_search_artists
import simpmusic.composeapp.generated.resources.xyz_search_failed
import simpmusic.composeapp.generated.resources.xyz_search_songs

// How long typing has to pause before the search runs, so a word is not searched letter by letter.
private const val SEARCH_DEBOUNCE_MS = 450L

/** The page padding: the shell's insets plus the launcher's gutter. */
@Composable
private fun pagePadding(innerPadding: PaddingValues): PaddingValues {
    val direction = LocalLayoutDirection.current
    return PaddingValues(
        start = innerPadding.calculateStartPadding(direction) + 16.dp,
        end = innerPadding.calculateEndPadding(direction) + 16.dp,
        top = innerPadding.calculateTopPadding() + 16.dp,
        bottom = innerPadding.calculateBottomPadding() + 16.dp,
    )
}

/**
 * Search text that runs [search] once typing pauses, or at once from the keyboard's search key.
 * [submitted] is what the results on screen answer; blank means "show the list, not results".
 */
private class DebouncedSearch(
    val query: String,
    val submitted: String,
    val onQueryChange: (String) -> Unit,
    val onSubmit: () -> Unit,
)

@Composable
private fun rememberDebouncedSearch(search: (String) -> Unit): DebouncedSearch {
    var query by rememberSaveable { mutableStateOf("") }
    var submitted by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(query) {
        if (query.isBlank()) {
            submitted = ""
            return@LaunchedEffect
        }
        if (query == submitted) return@LaunchedEffect
        delay(SEARCH_DEBOUNCE_MS)
        submitted = query
        search(query)
    }
    return DebouncedSearch(
        query = query,
        submitted = submitted,
        onQueryChange = { query = it },
        onSubmit = {
            if (query.isNotBlank()) {
                submitted = query
                search(query)
            }
        },
    )
}

/**
 * Songs: a search bar, and below it the songs played most (the song table's own play time, kept
 * whether or not listening history is on). Searching swaps the list for YouTube Music's song results.
 */
@Composable
fun XyzSongsScreen(
    innerPadding: PaddingValues,
    libraryViewModel: LibraryDynamicPlaylistViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val mostPlayed by libraryViewModel.listMostPlayedSong.collectAsStateWithLifecycle()
    val searchState by searchViewModel.searchScreenState.collectAsStateWithLifecycle()
    val searchUi by searchViewModel.searchScreenUIState.collectAsStateWithLifecycle()
    val nowPlaying by sharedViewModel.nowPlayingState.collectAsStateWithLifecycle()
    val currentVideoId = nowPlaying?.songEntity?.videoId
    val search = rememberDebouncedSearch { searchViewModel.searchSongs(it) }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = pagePadding(innerPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item(key = "search") {
            XyzSearchBar(
                query = search.query,
                onQueryChange = search.onQueryChange,
                placeholder = stringResource(Res.string.xyz_search_songs),
                onSearch = search.onSubmit,
                modifier = Modifier.padding(bottom = 16.dp),
            )
        }
        if (search.submitted.isNotBlank()) {
            searchStatus(searchUi, empty = searchState.searchSongsResult.isEmpty())
            if (searchUi == SearchScreenUIState.Success) {
                items(searchState.searchSongsResult, key = { "s${it.videoId}" }) { result ->
                    SongFullWidthItems(
                        track = result.toTrack(),
                        isPlaying = result.videoId == currentVideoId,
                        modifier = Modifier.fillMaxWidth(),
                        onClickListener = { playSearchResult(searchViewModel, result, search.submitted) },
                        onAddToQueue = { sharedViewModel.addListToQueue(arrayListOf(result.toTrack())) },
                    )
                }
            }
        } else {
            item(key = "header") {
                XyzSectionHeader(
                    title = stringResource(Res.string.xyz_most_played_songs),
                    subtitle = if (mostPlayed.isEmpty()) null else pluralStringResource(Res.plurals.liked_songs_count, mostPlayed.size, mostPlayed.size),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
                    if (mostPlayed.isNotEmpty()) {
                        PlayAllAndShuffle(
                            onPlayAll = { libraryViewModel.playAll(LibraryDynamicPlaylistType.MostPlayed) },
                            onShuffle = { libraryViewModel.shuffle(LibraryDynamicPlaylistType.MostPlayed) },
                        )
                    }
                }
            }
            if (mostPlayed.isEmpty()) {
                item(key = "empty") { XyzEmptyText(stringResource(Res.string.xyz_empty_songs)) }
            }
            items(mostPlayed, key = { it.videoId }) { song ->
                SongFullWidthItems(
                    songEntity = song,
                    isPlaying = song.videoId == currentVideoId,
                    modifier = Modifier.fillMaxWidth(),
                    onClickListener = { libraryViewModel.playSong(song.videoId, LibraryDynamicPlaylistType.MostPlayed) },
                    onAddToQueue = { sharedViewModel.addListToQueue(arrayListOf(song.toTrack())) },
                )
            }
        }
    }
}

/** A song from search plays as its radio, the way the stock search screen starts it. */
private fun playSearchResult(
    searchViewModel: SearchViewModel,
    result: SongsResult,
    query: String,
) {
    val firstTrack = result.toTrack()
    searchViewModel.setQueueData(
        QueueData.Data(
            listTracks = arrayListOf(firstTrack),
            firstPlayedTrack = firstTrack,
            playlistId = "RDAMVM${result.videoId}",
            playlistName = "\"$query\"",
            playlistType = PlaylistType.RADIO,
            continuation = null,
        ),
    )
    searchViewModel.loadMediaItem(firstTrack, Config.SONG_CLICK)
}

/** Loading, failed and "nothing found" rows above search results. */
private fun LazyListScope.searchStatus(
    state: SearchScreenUIState,
    empty: Boolean,
) {
    when {
        state == SearchScreenUIState.Loading ->
            item(key = "loading") {
                Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = xyzPalette().accent)
                }
            }
        state == SearchScreenUIState.Error -> item(key = "error") { XyzEmptyText(stringResource(Res.string.xyz_search_failed)) }
        state == SearchScreenUIState.Success && empty -> item(key = "none") { XyzEmptyText(stringResource(Res.string.xyz_no_results)) }
    }
}

@Composable
private fun PlayAllAndShuffle(
    onPlayAll: () -> Unit,
    onShuffle: () -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        XyzPillButton(stringResource(Res.string.play_all), filled = true, onClick = onPlayAll, icon = SimpIcons.PlayArrow)
        XyzPillButton(stringResource(Res.string.shuffle), filled = false, onClick = onShuffle, icon = SimpIcons.Shuffle)
    }
}

/** An artist the user plays, as the songs credit them, with the play time summed over their songs. */
private data class PlayedArtist(
    val channelId: String,
    val name: String,
    val playTime: Long,
    val fallbackThumbnail: String?,
)

/**
 * The artists behind the most played songs, ranked by the time spent on their songs. A song credited
 * to several artists counts for each. Blank ids are dropped: they cannot open an artist page.
 */
private fun playedArtists(songs: List<SongEntity>): List<PlayedArtist> {
    val byId = LinkedHashMap<String, PlayedArtist>()
    songs.forEach { song ->
        val names = song.artistName.orEmpty()
        song.artistId.orEmpty().forEachIndexed { i, id ->
            val name = names.getOrNull(i)
            if (id.isBlank() || name.isNullOrBlank()) return@forEachIndexed
            val known = byId[id]
            byId[id] =
                known?.copy(playTime = known.playTime + song.totalPlayTime)
                    ?: PlayedArtist(id, name, song.totalPlayTime, song.thumbnails)
        }
    }
    return byId.values.sortedByDescending { it.playTime }.take(MAX_ARTISTS)
}

private const val MAX_ARTISTS = 30

/**
 * Artists: a search bar, and below it the artists played most. Searching swaps the grid for YouTube
 * Music's artist results; either opens the artist's page.
 */
@Composable
fun XyzArtistsScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    libraryViewModel: LibraryDynamicPlaylistViewModel = koinViewModel(),
    searchViewModel: SearchViewModel = koinViewModel(),
) {
    val mostPlayed by libraryViewModel.listMostPlayedSong.collectAsStateWithLifecycle()
    val artists = remember(mostPlayed) { playedArtists(mostPlayed) }
    val searchState by searchViewModel.searchScreenState.collectAsStateWithLifecycle()
    val searchUi by searchViewModel.searchScreenUIState.collectAsStateWithLifecycle()
    val search = rememberDebouncedSearch { searchViewModel.searchArtists(it) }
    val fullLine: LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(ArtistTileSize + 24.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = pagePadding(innerPadding),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item(key = "search", span = fullLine) {
            XyzSearchBar(
                query = search.query,
                onQueryChange = search.onQueryChange,
                placeholder = stringResource(Res.string.xyz_search_artists),
                onSearch = search.onSubmit,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        if (search.submitted.isNotBlank()) {
            val results = searchState.searchArtistsResult
            when {
                searchUi == SearchScreenUIState.Loading ->
                    item(key = "loading", span = fullLine) {
                        Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = xyzPalette().accent)
                        }
                    }
                searchUi == SearchScreenUIState.Error ->
                    item(key = "error", span = fullLine) { XyzEmptyText(stringResource(Res.string.xyz_search_failed)) }
                searchUi == SearchScreenUIState.Success && results.isEmpty() ->
                    item(key = "none", span = fullLine) { XyzEmptyText(stringResource(Res.string.xyz_no_results)) }
                searchUi == SearchScreenUIState.Success ->
                    items(results, key = { "a${it.browseId}" }, span = { GridItemSpan(maxLineSpan) }) { result ->
                        ArtistFullWidthItems(
                            data = result,
                            onClickListener = { navController.navigate(ArtistDestination(result.browseId)) },
                        )
                    }
            }
        } else {
            item(key = "header", span = fullLine) {
                XyzSectionHeader(stringResource(Res.string.xyz_most_played_artists))
            }
            if (artists.isEmpty()) {
                item(key = "empty", span = fullLine) { XyzEmptyText(stringResource(Res.string.xyz_empty_artists)) }
            }
            items(artists, key = { it.channelId }) { artist ->
                StoredArtistTile(
                    channelId = artist.channelId,
                    name = artist.name,
                    fallbackThumbnail = artist.fallbackThumbnail,
                ) { navController.navigate(ArtistDestination(artist.channelId)) }
            }
        }
    }
}

/** Favorites: the artists the user follows, then the songs they liked (the heart). */
@Composable
fun XyzFavoritesScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    libraryViewModel: LibraryDynamicPlaylistViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val p = xyzPalette()
    val liked by libraryViewModel.listFavoriteSong.collectAsStateWithLifecycle()
    val followed by libraryViewModel.listFollowedArtist.collectAsStateWithLifecycle()
    val nowPlaying by sharedViewModel.nowPlayingState.collectAsStateWithLifecycle()
    val currentVideoId = nowPlaying?.songEntity?.videoId

    LazyColumn(Modifier.fillMaxSize(), contentPadding = pagePadding(innerPadding), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        item(key = "title") {
            Text(stringResource(Res.string.xyz_favorites), style = typo().titleLarge, color = p.text, modifier = Modifier.padding(bottom = 12.dp))
        }
        item(key = "artists_header") {
            XyzSectionHeader(
                title = stringResource(Res.string.artists),
                subtitle = if (followed.isEmpty()) null else followed.size.toString(),
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item(key = "artists") {
            if (followed.isEmpty()) {
                XyzEmptyText(stringResource(Res.string.xyz_empty_favorite_artists))
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    items(followed, key = { it.channelId }) { artist ->
                        ArtistTile(name = artist.name, thumbnail = artist.thumbnails) {
                            navController.navigate(ArtistDestination(artist.channelId))
                        }
                    }
                }
            }
        }
        item(key = "songs_header") {
            XyzSectionHeader(
                title = stringResource(Res.string.liked_songs),
                subtitle = if (liked.isEmpty()) null else pluralStringResource(Res.plurals.liked_songs_count, liked.size, liked.size),
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
            ) {
                if (liked.isNotEmpty()) {
                    PlayAllAndShuffle(
                        onPlayAll = { libraryViewModel.playAll(LibraryDynamicPlaylistType.Favorite) },
                        onShuffle = { libraryViewModel.shuffle(LibraryDynamicPlaylistType.Favorite) },
                    )
                }
            }
        }
        if (liked.isEmpty()) {
            item(key = "songs_empty") { XyzEmptyText(stringResource(Res.string.xyz_empty_favorite_songs)) }
        }
        items(liked, key = { it.videoId }) { song ->
            SongFullWidthItems(
                songEntity = song,
                isPlaying = song.videoId == currentVideoId,
                modifier = Modifier.fillMaxWidth(),
                onClickListener = { libraryViewModel.playSong(song.videoId, LibraryDynamicPlaylistType.Favorite) },
                onAddToQueue = { sharedViewModel.addListToQueue(arrayListOf(song.toTrack())) },
            )
        }
    }
}

private val ArtistTileSize: Dp = 112.dp

/** A round artist picture with the name under it. */
@Composable
private fun ArtistTile(
    name: String,
    thumbnail: String?,
    onClick: () -> Unit,
) {
    val p = xyzPalette()
    Column(
        Modifier
            .width(ArtistTileSize + 16.dp)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        XyzArtwork(thumbnail, Modifier.width(ArtistTileSize).aspectRatio(1f), round = true)
        Spacer(Modifier.height(8.dp))
        Text(
            name,
            style = typo().labelMedium,
            color = p.text,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * [ArtistTile] for an artist known only from song credits: the stored artist's picture once the app
 * has one (opening an artist page stores it), the artwork of their most played song until then.
 */
@Composable
private fun StoredArtistTile(
    channelId: String,
    name: String,
    fallbackThumbnail: String?,
    artistRepository: ArtistRepository = koinInject(),
    onClick: () -> Unit,
) {
    val stored by remember(channelId) { artistRepository.getArtistById(channelId) }.collectAsStateWithLifecycle(null)
    ArtistTile(name = stored?.name ?: name, thumbnail = stored?.thumbnails ?: fallbackThumbnail, onClick = onClick)
}
