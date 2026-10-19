package com.maxrave.simpmusic.ui.screen.xyz

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.player.GenericMediaItem
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.DataStoreManager.Values.TRUE
import com.maxrave.domain.mediaservice.handler.MediaPlayerHandler
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.mediaservice.handler.RepeatState
import com.maxrave.domain.utils.connectArtists
import com.maxrave.domain.utils.toListName
import com.maxrave.simpmusic.extension.LeftToRight
import com.maxrave.simpmusic.extension.elapsedLabel
import com.maxrave.simpmusic.extension.formatDuration
import com.maxrave.simpmusic.extension.lengthLabel
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.FavoriteBorder
import com.maxrave.simpmusic.ui.icon.GraphicEq
import com.maxrave.simpmusic.ui.icon.Pause
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.Repeat
import com.maxrave.simpmusic.ui.icon.RepeatOne
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.icon.SkipNext
import com.maxrave.simpmusic.ui.icon.SkipPrevious
import com.maxrave.simpmusic.ui.navigation.destination.login.LoginDestination
import com.maxrave.simpmusic.ui.theme.XyzPalette
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.UIEvent
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.liked_songs_count
import simpmusic.composeapp.generated.resources.log_in_to_YouTube
import simpmusic.composeapp.generated.resources.now_playing
import simpmusic.composeapp.generated.resources.shuffle
import simpmusic.composeapp.generated.resources.xyz_next
import simpmusic.composeapp.generated.resources.xyz_nothing_playing
import simpmusic.composeapp.generated.resources.xyz_nothing_playing_hint
import simpmusic.composeapp.generated.resources.xyz_play_pause
import simpmusic.composeapp.generated.resources.xyz_previous
import simpmusic.composeapp.generated.resources.xyz_queue_empty
import simpmusic.composeapp.generated.resources.xyz_repeat
import simpmusic.composeapp.generated.resources.xyz_up_next

/**
 * Home in the XYZ look IS the player, laid out like XYZ Glazba in the launcher: the cover with what is
 * playing beside it, the seek bar and transport under them, and the queue ("Up next") on the right.
 * Upright (a phone) the same blocks stack and the queue follows in the same list. The only thing above
 * it is the YouTube account, signed in or the way to sign in.
 */
@Composable
fun XyzHomeScreen(
    navController: NavController,
    innerPadding: PaddingValues,
    sharedViewModel: SharedViewModel = koinInject(),
    mediaPlayerHandler: MediaPlayerHandler = koinInject(),
) {
    val p = xyzPalette()
    val nowPlaying by sharedViewModel.nowPlayingState.collectAsStateWithLifecycle()
    val screenData by sharedViewModel.nowPlayingScreenData.collectAsStateWithLifecycle()
    val controller by sharedViewModel.controllerState.collectAsStateWithLifecycle()
    val timeline by sharedViewModel.timeline.collectAsStateWithLifecycle()
    val queueData by mediaPlayerHandler.queueData.collectAsStateWithLifecycle()
    val queue = queueData?.data?.listTracks.orEmpty()

    val item = nowPlaying?.mediaItem
    val hasTrack = item != null && item != GenericMediaItem.EMPTY
    val currentVideoId = nowPlaying?.songEntity?.videoId ?: nowPlaying?.track?.videoId
    val title = if (hasTrack) screenData.nowPlayingTitle else stringResource(Res.string.xyz_nothing_playing)
    val artist = if (hasTrack) screenData.artistName else stringResource(Res.string.xyz_nothing_playing_hint)
    val album = if (hasTrack) nowPlaying?.songEntity?.albumName ?: nowPlaying?.track?.album?.name else null

    val info =
        NowPlayingInfo(
            hasTrack = hasTrack,
            title = title,
            artist = artist,
            album = album,
            cover = if (hasTrack) screenData.thumbnailURL else null,
            liked = controller.isLiked,
        )
    val onUiEvent: (UIEvent) -> Unit = { sharedViewModel.onUIEvent(it) }
    val onPlayQueueItem: (Int) -> Unit = { index -> mediaPlayerHandler.playMediaItemInMediaSource(index) }

    val listState = rememberLazyListState()
    // Keep the playing track in view as the queue advances, one row of context above it.
    LaunchedEffect(currentVideoId, queue.size) {
        val index = mediaPlayerHandler.currentOrderIndex()
        if (index >= 0 && index < queue.size) listState.animateScrollToItem((index - 1).coerceAtLeast(0))
    }
    // An endless queue or a radio continues when the list runs out, as in the stock queue sheet.
    LaunchedEffect(listState, queueData?.queueState) {
        snapshotFlow {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - 3
        }.distinctUntilChanged().collect { nearEnd ->
            if (nearEnd && queueData?.queueState == QueueData.StateSource.STATE_INITIALIZED) mediaPlayerHandler.loadMore()
        }
    }

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        val landscape = maxWidth > maxHeight
        if (landscape) {
            Column(Modifier.fillMaxSize().padding(start = 8.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
                XyzAccountRow(navController)
                Spacer(Modifier.height(10.dp))
                Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        Row(Modifier.weight(1f).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            XyzArtwork(
                                url = info.cover,
                                corner = 22.dp,
                                modifier =
                                    Modifier
                                        .fillMaxHeight()
                                        .widthIn(max = this@BoxWithConstraints.maxWidth * 0.36f)
                                        .aspectRatio(1f, matchHeightConstraintsFirst = true),
                            )
                            Spacer(Modifier.width(24.dp))
                            TrackInfo(info, onUiEvent, Modifier.weight(1f), large = true)
                        }
                        Spacer(Modifier.height(14.dp))
                        PlaybackControls(
                            hasTrack = hasTrack,
                            isPlaying = controller.isPlaying,
                            isShuffle = controller.isShuffle,
                            repeat = controller.repeatState,
                            timeline = timeline,
                            onUiEvent = onUiEvent,
                        )
                    }
                    XyzCard(
                        Modifier
                            .width((this@BoxWithConstraints.maxWidth * 0.34f).coerceIn(250.dp, 380.dp))
                            .fillMaxHeight(),
                    ) {
                        LazyColumn(state = listState, contentPadding = PaddingValues(8.dp)) {
                            upNext(queue, currentVideoId, p, onPlayQueueItem)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            ) {
                item(key = "account") { XyzAccountRow(navController) }
                item(key = "cover") {
                    Box(Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
                        XyzArtwork(
                            url = info.cover,
                            corner = 22.dp,
                            modifier = Modifier.fillMaxWidth(0.86f).aspectRatio(1f),
                        )
                    }
                }
                item(key = "info") { TrackInfo(info, onUiEvent, Modifier.fillMaxWidth(), large = false) }
                item(key = "controls") {
                    Column {
                        Spacer(Modifier.height(16.dp))
                        PlaybackControls(
                            hasTrack = hasTrack,
                            isPlaying = controller.isPlaying,
                            isShuffle = controller.isShuffle,
                            repeat = controller.repeatState,
                            timeline = timeline,
                            onUiEvent = onUiEvent,
                        )
                        Spacer(Modifier.height(20.dp))
                    }
                }
                upNext(queue, currentVideoId, p, onPlayQueueItem)
            }
        }
    }
}

private class NowPlayingInfo(
    val hasTrack: Boolean,
    val title: String,
    val artist: String,
    val album: String?,
    val cover: String?,
    val liked: Boolean,
)

/** "NOW PLAYING", title, artist and album, with the heart at the end of the label row. */
@Composable
private fun TrackInfo(
    info: NowPlayingInfo,
    onUiEvent: (UIEvent) -> Unit,
    modifier: Modifier,
    large: Boolean,
) {
    val p = xyzPalette()
    Column(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(Res.string.now_playing).toUpperCase(Locale.current),
                style = typo().bodySmall,
                color = p.textDim,
                letterSpacing = 1.2.sp,
                modifier = Modifier.weight(1f),
            )
            if (info.hasTrack) {
                IconButton(onClick = { onUiEvent(UIEvent.ToggleLike) }) {
                    Icon(
                        if (info.liked) SimpIcons.Favorite else SimpIcons.FavoriteBorder,
                        contentDescription = null,
                        tint = if (info.liked) p.accent else p.textDim,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
        }
        Text(
            info.title,
            style = typo().titleLarge.copy(fontSize = if (large) 30.sp else 24.sp),
            color = p.text,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(4.dp))
        Text(info.artist, style = typo().bodyLarge, color = lerp(p.textDim, p.text, 0.35f), maxLines = 1, overflow = TextOverflow.Ellipsis)
        if (!info.album.isNullOrBlank()) {
            Text(info.album, style = typo().bodyMedium, color = p.textDim, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** Seek bar with elapsed and remaining time, then shuffle · previous · play/pause · next · repeat. */
@Composable
private fun PlaybackControls(
    hasTrack: Boolean,
    isPlaying: Boolean,
    isShuffle: Boolean,
    repeat: RepeatState,
    timeline: com.maxrave.domain.data.model.streams.TimeLine,
    onUiEvent: (UIEvent) -> Unit,
) {
    val p = xyzPalette()
    // Time runs left to right whatever the language, like every other player in the app.
    LeftToRight {
        Column(Modifier.fillMaxWidth()) {
            val progress =
                if (timeline.total > 0 && timeline.current >= 0) (timeline.current.toFloat() / timeline.total).coerceIn(0f, 1f) else 0f
            var dragging by remember { mutableStateOf<Float?>(null) }
            val shown = dragging ?: progress
            XyzSeekBar(
                progress = shown,
                enabled = hasTrack && !timeline.isLive,
                onDrag = { dragging = it },
                onSeek = {
                    dragging = null
                    onUiEvent(UIEvent.UpdateProgress(it * 100f))
                },
                onCancel = { dragging = null },
            )
            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
                val elapsed = if (hasTrack) timeline.elapsedLabel(shown) else ""
                Text(elapsed, style = typo().bodySmall, color = p.textDim)
                Spacer(Modifier.weight(1f))
                val remaining =
                    when {
                        !hasTrack -> ""
                        timeline.isLive || timeline.total <= 0 -> timeline.lengthLabel()
                        else -> "-" + formatDuration((timeline.total * (1f - shown)).toLong())
                    }
                Text(remaining, style = typo().bodySmall, color = p.textDim)
            }
            Spacer(Modifier.height(6.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ToggleButton(SimpIcons.Shuffle, stringResource(Res.string.shuffle), on = isShuffle) { onUiEvent(UIEvent.Shuffle) }
                TileButton(SimpIcons.SkipPrevious, stringResource(Res.string.xyz_previous), enabled = hasTrack) { onUiEvent(UIEvent.Previous) }
                PlayButton(isPlaying = isPlaying, enabled = hasTrack) { onUiEvent(UIEvent.PlayPause) }
                TileButton(SimpIcons.SkipNext, stringResource(Res.string.xyz_next), enabled = hasTrack) { onUiEvent(UIEvent.Next) }
                ToggleButton(
                    if (repeat == RepeatState.One) SimpIcons.RepeatOne else SimpIcons.Repeat,
                    stringResource(Res.string.xyz_repeat),
                    on = repeat != RepeatState.None,
                ) { onUiEvent(UIEvent.Repeat) }
            }
        }
    }
}

/** The launcher's seek bar: a thin rounded track, the played part in the accent and a round thumb. */
@Composable
private fun XyzSeekBar(
    progress: Float,
    enabled: Boolean,
    onDrag: (Float) -> Unit,
    onSeek: (Float) -> Unit,
    onCancel: () -> Unit,
) {
    val p = xyzPalette()
    val latestDrag by rememberUpdatedState(onDrag)
    val latestSeek by rememberUpdatedState(onSeek)
    val latestCancel by rememberUpdatedState(onCancel)
    var lastDrag by remember { mutableStateOf(0f) }
    val thumb = lerp(p.accent, Color.White, 0.75f)
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(30.dp)
            .padding(horizontal = 10.dp)
            .then(
                if (enabled) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures { latestSeek((it.x / size.width).coerceIn(0f, 1f)) }
                        }.pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragStart = {
                                    lastDrag = (it.x / size.width).coerceIn(0f, 1f)
                                    latestDrag(lastDrag)
                                },
                                onDragEnd = { latestSeek(lastDrag) },
                                onDragCancel = { latestCancel() },
                            ) { change, _ ->
                                change.consume()
                                lastDrag = (change.position.x / size.width).coerceIn(0f, 1f)
                                latestDrag(lastDrag)
                            }
                        }
                } else {
                    Modifier
                },
            ),
    ) {
        val h = 5.dp.toPx()
        val y = size.height / 2f
        drawRoundRect(p.tile, topLeft = Offset(0f, y - h / 2f), size = Size(size.width, h), cornerRadius = CornerRadius(h / 2f))
        if (enabled) {
            val x = size.width * progress.coerceIn(0f, 1f)
            drawRoundRect(p.accent, topLeft = Offset(0f, y - h / 2f), size = Size(x, h), cornerRadius = CornerRadius(h / 2f))
            drawCircle(p.accent.copy(alpha = 0.35f), radius = 13.dp.toPx(), center = Offset(x, y))
            drawCircle(thumb, radius = 9.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
private fun ToggleButton(
    icon: ImageVector,
    label: String,
    on: Boolean,
    onClick: () -> Unit,
) {
    val p = xyzPalette()
    IconButton(onClick = onClick, modifier = Modifier.size(52.dp)) {
        Icon(icon, contentDescription = label, tint = if (on) p.accent else p.textDim, modifier = Modifier.size(26.dp))
    }
}

/** Previous / next: a round tile in the launcher's tile colour. */
@Composable
private fun TileButton(
    icon: ImageVector,
    label: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val p = xyzPalette()
    Box(
        Modifier
            .size(64.dp)
            .clip(CircleShape)
            .background(p.tile)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = label, tint = if (enabled) p.text else p.textDim, modifier = Modifier.size(30.dp))
    }
}

/** Play/pause: the launcher's big button, a dark disc inside a ring running from accent to accent2. */
@Composable
private fun PlayButton(
    isPlaying: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val p = xyzPalette()
    Box(
        Modifier
            .size(PlayButtonSize)
            .clip(CircleShape)
            .background(p.cardTop)
            .border(4.dp, Brush.sweepGradient(listOf(p.accent, p.accent2, p.accent)), CircleShape)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (isPlaying) SimpIcons.Pause else SimpIcons.PlayArrow,
            contentDescription = stringResource(Res.string.xyz_play_pause),
            tint = p.text,
            modifier = Modifier.size(42.dp),
        )
    }
}

private val PlayButtonSize: Dp = 84.dp

/** "Up next" and the queue, the playing row lit with the accent like XYZ Glazba's list. */
private fun LazyListScope.upNext(
    queue: List<Track>,
    currentVideoId: String?,
    p: XyzPalette,
    onPlay: (Int) -> Unit,
) {
    item(key = "up_next_header") {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(Res.string.xyz_up_next), style = typo().titleMedium, color = p.text, modifier = Modifier.weight(1f))
            if (queue.isNotEmpty()) {
                Text(pluralStringResource(Res.plurals.liked_songs_count, queue.size, queue.size), style = typo().bodySmall, color = p.textDim)
            }
        }
    }
    if (queue.isEmpty()) {
        item(key = "up_next_empty") { XyzEmptyText(stringResource(Res.string.xyz_queue_empty)) }
    }
    itemsIndexed(queue, key = { index, track -> "q$index-${track.videoId}" }) { index, track ->
        QueueRow(track, current = track.videoId == currentVideoId, p = p) { onPlay(index) }
    }
}

@Composable
private fun QueueRow(
    track: Track,
    current: Boolean,
    p: XyzPalette,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(p.corner(14.dp))
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clip(shape)
            .background(if (current) p.accent.copy(alpha = 0.22f) else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(24.dp), contentAlignment = Alignment.Center) {
            if (current) Icon(SimpIcons.GraphicEq, contentDescription = null, tint = p.accent, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(6.dp))
        XyzArtwork(track.thumbnails?.lastOrNull()?.url, Modifier.size(46.dp), corner = 10.dp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                track.title,
                style = typo().labelMedium,
                color = if (current) lerp(p.accent, Color.White, 0.25f) else p.text,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                track.artists.toListName().connectArtists(),
                style = typo().bodySmall,
                color = p.textDim,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        val duration = track.duration
        if (!duration.isNullOrBlank()) {
            Spacer(Modifier.width(8.dp))
            Text(duration, style = typo().bodySmall, color = p.textDim)
        }
    }
}

/**
 * The only thing above the player: the YouTube account. Signed in, its picture and name; signed out,
 * the button that opens the YouTube sign-in.
 */
@Composable
private fun XyzAccountRow(
    navController: NavController,
    sharedViewModel: SharedViewModel = koinInject(),
    dataStoreManager: DataStoreManager = koinInject(),
) {
    val p = xyzPalette()
    val loggedIn by sharedViewModel.getYouTubeLoggedIn().collectAsStateWithLifecycle(DataStoreManager.FALSE)
    val accountName by dataStoreManager.getString("AccountName").collectAsStateWithLifecycle(null)
    val accountThumb by dataStoreManager.getString("AccountThumbUrl").collectAsStateWithLifecycle(null)
    val signedIn by remember(loggedIn, accountName) { derivedStateOf { loggedIn == TRUE && !accountName.isNullOrBlank() } }
    Row(Modifier.fillMaxWidth().height(44.dp), verticalAlignment = Alignment.CenterVertically) {
        Spacer(Modifier.weight(1f))
        if (signedIn) {
            XyzCard(corner = 22.dp, glow = false) {
                Row(Modifier.padding(start = 6.dp, end = 16.dp, top = 6.dp, bottom = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    XyzArtwork(accountThumb, Modifier.size(32.dp), round = true)
                    Spacer(Modifier.width(10.dp))
                    Text(accountName.orEmpty(), style = typo().labelMedium, color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        } else {
            XyzPillButton(
                text = stringResource(Res.string.log_in_to_YouTube),
                filled = true,
                onClick = { navController.navigate(LoginDestination) },
            )
        }
    }
}
