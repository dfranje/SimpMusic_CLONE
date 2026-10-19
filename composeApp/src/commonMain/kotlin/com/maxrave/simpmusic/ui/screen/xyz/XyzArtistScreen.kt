package com.maxrave.simpmusic.ui.screen.xyz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.Config
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.connectArtists
import com.maxrave.domain.utils.toListName
import com.maxrave.domain.utils.toSongEntity
import com.maxrave.simpmusic.ui.component.NowPlayingBottomSheet
import com.maxrave.simpmusic.ui.icon.ArrowForwardIos
import com.maxrave.simpmusic.ui.icon.Check
import com.maxrave.simpmusic.ui.icon.CheckCircle
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.FavoriteBorder
import com.maxrave.simpmusic.ui.icon.GraphicEq
import com.maxrave.simpmusic.ui.icon.MoreVert
import com.maxrave.simpmusic.ui.icon.PersonAdd
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.AlbumDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.MoreAlbumsDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.screen.library.LibraryDynamicPlaylistType
import com.maxrave.simpmusic.ui.theme.XyzPalette
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.ArtistScreenData
import com.maxrave.simpmusic.viewModel.ArtistScreenState
import com.maxrave.simpmusic.viewModel.ArtistViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.albums
import simpmusic.composeapp.generated.resources.error
import simpmusic.composeapp.generated.resources.follow
import simpmusic.composeapp.generated.resources.followed
import simpmusic.composeapp.generated.resources.popular
import simpmusic.composeapp.generated.resources.singles
import simpmusic.composeapp.generated.resources.xyz_artist_badge
import simpmusic.composeapp.generated.resources.xyz_artist_liked_songs
import simpmusic.composeapp.generated.resources.xyz_artist_no_liked_songs
import simpmusic.composeapp.generated.resources.xyz_play
import simpmusic.composeapp.generated.resources.xyz_popular_songs
import simpmusic.composeapp.generated.resources.xyz_see_all

// How many releases the Albums card shows before "See all": two rows of three.
private const val RELEASE_COLUMNS = 3
private const val MAX_RELEASES = 6

/** A release on the Albums card, an album or (when the artist has none) a single. */
private class XyzRelease(
    val browseId: String,
    val title: String,
    val year: String,
    val cover: String?,
)

/**
 * The artist page of the XYZ shell: a hero banner (picture, name, listeners, description, Play,
 * Follow and the liked-songs heart) over two cards, Popular songs on the left and Albums on the
 * right. Upright the two cards stack. It reads the stock [ArtistViewModel], so playing, following
 * and every link behave as on the stock page.
 */
@Composable
fun XyzArtistScreen(
    channelId: String,
    navController: NavController,
    innerPadding: PaddingValues,
    viewModel: ArtistViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val p = xyzPalette()
    val state by viewModel.artistScreenState.collectAsStateWithLifecycle()
    val isFollowed by viewModel.followed.collectAsStateWithLifecycle()
    val likedSongCount by viewModel.likedSongCount.collectAsStateWithLifecycle()
    val nowPlaying by sharedViewModel.nowPlayingState.collectAsStateWithLifecycle()
    val playingId = nowPlaying?.songEntity?.videoId ?: nowPlaying?.track?.videoId
    var menuTrack by remember { mutableStateOf<Track?>(null) }

    LaunchedEffect(channelId) {
        if (channelId != viewModel.artistScreenState.value.data.channelId) viewModel.browseArtist(channelId)
    }

    when (val current = state) {
        is ArtistScreenState.Loading -> {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = p.accent)
            }
        }

        is ArtistScreenState.Error -> {
            val error = current.message ?: stringResource(Res.string.error)
            LaunchedEffect(current) {
                viewModel.makeToast(error)
                navController.navigateUp()
            }
        }

        is ArtistScreenState.Success -> {
            val data = current.data
            val popularTitle = "\"${data.title.orEmpty()}\" ${stringResource(Res.string.popular)}"
            // A popular song plays as its own radio, the way the stock page plays it.
            val playSong: (Track) -> Unit = { song ->
                viewModel.setQueueData(
                    QueueData.Data(
                        listTracks = arrayListOf(song),
                        firstPlayedTrack = song,
                        playlistId = "RDAMVM${song.videoId}",
                        playlistName = popularTitle,
                        playlistType = PlaylistType.RADIO,
                        continuation = null,
                    ),
                )
                viewModel.loadMediaItem(song, type = Config.SONG_CLICK)
            }
            val onPlay: () -> Unit = {
                val radio = data.radioParam
                val first = data.popularSongs.firstOrNull()
                when {
                    radio != null -> viewModel.onRadioClick(radio)
                    first != null -> playSong(first)
                }
            }
            val noLikedSongs = stringResource(Res.string.xyz_artist_no_liked_songs)
            val onHeart: () -> Unit = {
                if (likedSongCount > 0) {
                    navController.navigate(
                        LibraryDynamicPlaylistDestination(type = LibraryDynamicPlaylistType.ArtistLiked(channelId).toStringParams()),
                    )
                } else {
                    viewModel.makeToast(noLikedSongs)
                }
            }
            val releases = data.releases()
            val onSeeAllReleases: (() -> Unit)? =
                data.channelId?.let { id ->
                    {
                        navController.navigate(
                            MoreAlbumsDestination(
                                id = "MPAD$id",
                                type = if (releases.second) MoreAlbumsDestination.ALBUM_TYPE else MoreAlbumsDestination.SINGLE_TYPE,
                            ),
                        )
                    }
                }
            val onSeeAllSongs: (() -> Unit)? = data.listSongParam?.let { id -> { navController.navigate(PlaylistDestination(id)) } }

            BoxWithConstraints(Modifier.fillMaxSize()) {
                val landscape = maxWidth > maxHeight
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = pagePadding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item(key = "hero") {
                        ArtistHero(
                            data = data,
                            landscape = landscape,
                            heroHeight = if (landscape) (maxHeight * 0.52f).coerceIn(260.dp, 360.dp) else 420.dp,
                            isFollowed = isFollowed,
                            hasLikedSongs = likedSongCount > 0,
                            onPlay = onPlay,
                            onFollow = { viewModel.updateFollowed(if (isFollowed) 0 else 1, channelId) },
                            onHeart = onHeart,
                        )
                    }
                    item(key = "cards") {
                        val songsCard: @Composable (Modifier) -> Unit = { modifier ->
                            PopularSongsCard(
                                songs = data.popularSongs,
                                playingId = playingId,
                                onSeeAll = onSeeAllSongs,
                                onPlay = playSong,
                                onMore = { menuTrack = it },
                                modifier = modifier,
                            )
                        }
                        val releasesCard: @Composable (Modifier) -> Unit = { modifier ->
                            ReleasesCard(
                                title = stringResource(if (releases.second) Res.string.albums else Res.string.singles),
                                releases = releases.first,
                                onSeeAll = onSeeAllReleases,
                                onOpen = { navController.navigate(AlbumDestination(browseId = it.browseId)) },
                                modifier = modifier,
                            )
                        }
                        val hasSongs = data.popularSongs.isNotEmpty()
                        val hasReleases = releases.first.isNotEmpty()
                        if (landscape && hasSongs && hasReleases) {
                            // Popular songs on the left, Albums on the right, both as tall as the taller.
                            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Max), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                songsCard(Modifier.weight(1.1f).fillMaxHeight())
                                releasesCard(Modifier.weight(1f).fillMaxHeight())
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                if (hasSongs) songsCard(Modifier.fillMaxWidth())
                                if (hasReleases) releasesCard(Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
            }

            menuTrack?.let { track ->
                NowPlayingBottomSheet(
                    onDismiss = { menuTrack = null },
                    navController = navController,
                    song = track.toSongEntity(),
                )
            }
        }
    }
}

/**
 * The releases the Albums card shows, and whether they are albums (`true`) or singles. An artist
 * with no albums gets their singles instead, so the card is not empty beside the songs.
 */
private fun ArtistScreenData.releases(): Pair<List<XyzRelease>, Boolean> {
    val albums = albums?.results.orEmpty()
    if (albums.isNotEmpty()) {
        return albums.map { XyzRelease(it.browseId, it.title, it.year, it.thumbnails.lastOrNull()?.url) } to true
    }
    val singles = singles?.results.orEmpty()
    return singles.map { XyzRelease(it.browseId, it.title, it.year, it.thumbnails.lastOrNull()?.url) } to false
}

@Composable
private fun ArtistHero(
    data: ArtistScreenData,
    landscape: Boolean,
    heroHeight: Dp,
    isFollowed: Boolean,
    hasLikedSongs: Boolean,
    onPlay: () -> Unit,
    onFollow: () -> Unit,
    onHeart: () -> Unit,
) {
    val p = xyzPalette()
    var descriptionOpen by remember { mutableStateOf(false) }
    XyzCard(Modifier.fillMaxWidth().height(heroHeight), corner = 26.dp) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(data.imageUrl)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alignment = if (landscape) Alignment.CenterEnd else Alignment.TopCenter,
            modifier = Modifier.fillMaxSize(),
        )
        // The picture fades into the card behind the text: from the left on a wide page, from the
        // bottom upright, and always a little along the bottom edge.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    if (landscape) {
                        Brush.horizontalGradient(
                            0f to p.cardTop,
                            0.38f to p.cardTop.copy(alpha = 0.88f),
                            0.72f to p.cardTop.copy(alpha = 0.15f),
                            1f to Color.Transparent,
                        )
                    } else {
                        Brush.verticalGradient(
                            0f to Color.Transparent,
                            0.35f to p.cardBottom.copy(alpha = 0.2f),
                            0.62f to p.cardBottom.copy(alpha = 0.92f),
                            1f to p.cardBottom,
                        )
                    },
                ),
        )
        Box(
            Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(0.7f to Color.Transparent, 1f to p.cardBottom.copy(alpha = 0.7f))),
        )
        Column(
            Modifier
                .align(if (landscape) Alignment.CenterStart else Alignment.BottomStart)
                .then(if (landscape) Modifier.fillMaxWidth(0.6f) else Modifier.fillMaxWidth())
                .padding(horizontal = 28.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ArtistBadge(p)
            Text(
                accentedName(data.title.orEmpty(), p),
                style = typo().titleLarge.copy(fontSize = if (landscape) 44.sp else 34.sp, lineHeight = if (landscape) 48.sp else 38.sp),
                color = p.text,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            val meta = listOfNotNull(data.subscribers?.takeIf { it.isNotBlank() }, data.playCount?.takeIf { it.isNotBlank() })
            if (meta.isNotEmpty()) {
                Text(meta.joinToString("  •  "), style = typo().bodyMedium, color = p.textDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            val description = data.description?.trim()
            if (!description.isNullOrEmpty()) {
                Text(
                    description,
                    style = typo().bodySmall,
                    color = p.textDim,
                    maxLines = if (descriptionOpen) 8 else 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 560.dp).clickable { descriptionOpen = !descriptionOpen },
                )
            }
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                XyzPillButton(
                    text = stringResource(Res.string.xyz_play),
                    filled = true,
                    onClick = onPlay,
                    icon = SimpIcons.PlayArrow,
                    modifier = Modifier.widthIn(min = 112.dp),
                )
                XyzPillButton(
                    text = stringResource(if (isFollowed) Res.string.followed else Res.string.follow),
                    filled = false,
                    onClick = onFollow,
                    icon = if (isFollowed) SimpIcons.Check else SimpIcons.PersonAdd,
                )
                HeartButton(filled = hasLikedSongs, onClick = onHeart, p = p)
            }
        }
    }
}

/** The artist's name with its last word in the accent, as on the launcher's music hero. */
private fun accentedName(
    name: String,
    p: XyzPalette,
) = buildAnnotatedString {
    val split = name.trimEnd().lastIndexOf(' ')
    if (split <= 0) {
        append(name)
    } else {
        append(name.substring(0, split + 1))
        withStyle(SpanStyle(color = p.accent)) { append(name.substring(split + 1)) }
    }
}

@Composable
private fun ArtistBadge(p: XyzPalette) {
    val shape = RoundedCornerShape(50)
    Row(
        Modifier
            .clip(shape)
            .background(p.accent.copy(alpha = 0.16f), shape)
            .border(1.dp, p.accent.copy(alpha = 0.45f), shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(SimpIcons.CheckCircle, contentDescription = null, tint = p.accent, modifier = Modifier.size(14.dp))
        Text(
            stringResource(Res.string.xyz_artist_badge).uppercase(),
            style = typo().bodySmall.copy(letterSpacing = 1.6.sp),
            color = p.accent,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/** A round button for the artist's songs the user has liked: filled once there is at least one. */
@Composable
private fun HeartButton(
    filled: Boolean,
    onClick: () -> Unit,
    p: XyzPalette,
) {
    Box(
        Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(p.cardBrush, CircleShape)
            .border(1.dp, if (filled) p.accent else p.border, CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (filled) SimpIcons.Favorite else SimpIcons.FavoriteBorder,
            contentDescription = stringResource(Res.string.xyz_artist_liked_songs),
            tint = if (filled) p.accent else p.text,
            modifier = Modifier.size(22.dp),
        )
    }
}

/** A card's title row, with "See all ›" at its end when there is more to see. */
@Composable
private fun CardHeader(
    title: String,
    onSeeAll: (() -> Unit)?,
) {
    val p = xyzPalette()
    Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = typo().titleMedium, color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
        if (onSeeAll != null) {
            Row(
                Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(stringResource(Res.string.xyz_see_all), style = typo().bodySmall, color = p.accent, fontWeight = FontWeight.SemiBold)
                Icon(SimpIcons.ArrowForwardIos, contentDescription = null, tint = p.accent, modifier = Modifier.size(12.dp))
            }
        }
    }
}

@Composable
private fun PopularSongsCard(
    songs: List<Track>,
    playingId: String?,
    onSeeAll: (() -> Unit)?,
    onPlay: (Track) -> Unit,
    onMore: (Track) -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = xyzPalette()
    XyzCard(modifier, corner = 22.dp, glow = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            CardHeader(stringResource(Res.string.xyz_popular_songs), onSeeAll)
            songs.forEachIndexed { index, song ->
                PopularSongRow(
                    number = index + 1,
                    song = song,
                    playing = song.videoId == playingId,
                    p = p,
                    onClick = { onPlay(song) },
                    onMore = { onMore(song) },
                )
            }
        }
    }
}

@Composable
private fun PopularSongRow(
    number: Int,
    song: Track,
    playing: Boolean,
    p: XyzPalette,
    onClick: () -> Unit,
    onMore: () -> Unit,
) {
    val shape = RoundedCornerShape(p.corner(14.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(shape)
            .background(if (playing) p.accent.copy(alpha = 0.18f) else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(start = 6.dp, top = 6.dp, bottom = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            number.toString().padStart(2, '0'),
            style = typo().labelMedium,
            color = if (playing) p.accent else p.textDim,
            modifier = Modifier.width(30.dp),
        )
        XyzArtwork(song.thumbnails?.lastOrNull()?.url, Modifier.size(48.dp), corner = 10.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                song.title,
                style = typo().labelMedium,
                color = if (playing) lerp(p.accent, Color.White, 0.25f) else p.text,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val subtitle = song.album?.name?.takeIf { it.isNotBlank() } ?: song.artists.toListName().connectArtists()
            Text(subtitle, style = typo().bodySmall, color = p.textDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (playing) {
            Icon(SimpIcons.GraphicEq, contentDescription = null, tint = p.accent, modifier = Modifier.padding(start = 8.dp).size(20.dp))
        }
        val length = song.lengthLabel()
        if (length != null) {
            Text(length, style = typo().bodySmall, color = p.textDim, modifier = Modifier.padding(start = 10.dp))
        }
        IconButton(onClick = onMore) {
            Icon(SimpIcons.MoreVert, contentDescription = null, tint = p.textDim)
        }
    }
}

/** "3:45" from the track's own text, else from its length in seconds; null when neither is known. */
private fun Track.lengthLabel(): String? {
    duration?.takeIf { it.isNotBlank() }?.let { return it }
    val seconds = durationSeconds?.takeIf { it > 0 } ?: return null
    return "${seconds / 60}:${(seconds % 60).toString().padStart(2, '0')}"
}

@Composable
private fun ReleasesCard(
    title: String,
    releases: List<XyzRelease>,
    onSeeAll: (() -> Unit)?,
    onOpen: (XyzRelease) -> Unit,
    modifier: Modifier = Modifier,
) {
    XyzCard(modifier, corner = 22.dp, glow = false) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            CardHeader(title, onSeeAll)
            releases.take(MAX_RELEASES).chunked(RELEASE_COLUMNS).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    row.forEach { release -> ReleaseTile(release, Modifier.weight(1f)) { onOpen(release) } }
                    // Keep a short last row on the grid instead of stretching its covers.
                    repeat(RELEASE_COLUMNS - row.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
private fun ReleaseTile(
    release: XyzRelease,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val p = xyzPalette()
    val shape = RoundedCornerShape(p.corner(14.dp))
    Column(modifier.clip(shape).clickable(onClick = onClick)) {
        XyzArtwork(release.cover, Modifier.fillMaxWidth().aspectRatio(1f), corner = 14.dp)
        Spacer(Modifier.height(6.dp))
        Text(release.title, style = typo().labelMedium, color = p.text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (release.year.isNotBlank()) {
            Text(release.year, style = typo().bodySmall, color = p.textDim, maxLines = 1)
        }
    }
}
