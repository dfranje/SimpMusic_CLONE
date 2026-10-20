package com.maxrave.simpmusic.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.common.Config
import com.maxrave.domain.data.entities.AlbumEntity
import com.maxrave.domain.data.entities.LocalPlaylistEntity
import com.maxrave.domain.data.entities.PlaylistEntity
import com.maxrave.domain.data.entities.PodcastsEntity
import com.maxrave.domain.data.model.browse.album.Track
import com.maxrave.domain.data.model.browse.artist.ResultAlbum
import com.maxrave.domain.data.model.browse.artist.ResultPlaylist
import com.maxrave.domain.data.model.browse.artist.ResultSingle
import com.maxrave.domain.data.model.home.Content
import com.maxrave.domain.data.model.searchResult.albums.AlbumsResult
import com.maxrave.domain.data.model.searchResult.playlists.PlaylistsResult
import com.maxrave.domain.data.model.streams.YouTubeWatchEndpoint
import com.maxrave.domain.data.type.ChartItem
import com.maxrave.domain.data.type.HomeContentType
import com.maxrave.domain.data.type.MonthlyRecapItem
import com.maxrave.domain.mediaservice.handler.PlaylistType
import com.maxrave.domain.mediaservice.handler.QueueData
import com.maxrave.domain.utils.connectArtists
import com.maxrave.domain.utils.toListName
import com.maxrave.domain.utils.toTrack
import com.maxrave.simpmusic.extension.getStringBlocking
import com.maxrave.simpmusic.ui.icon.PlayArrow
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.LocalForceDarkText
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.album
import simpmusic.composeapp.generated.resources.app_name
import simpmusic.composeapp.generated.resources.live_badge
import simpmusic.composeapp.generated.resources.description
import simpmusic.composeapp.generated.resources.playlist
import simpmusic.composeapp.generated.resources.radio
import simpmusic.composeapp.generated.resources.wrapped_recap_subtitle
import simpmusic.composeapp.generated.resources.you

/**
 * The card's hover play button. A song or video plays exactly as a tap on the card does, so every
 * one of them gets it. A playlist or album plays what YouTube's own play button points at, straight
 * from Home; a playlist card that came without one still plays by its id. Artists and podcasts,
 * which have nothing to play, get no button.
 */
private fun Content.hoverPlay(viewModel: BaseViewModel): (() -> Unit)? {
    if (!videoId.isNullOrEmpty()) return { playRadio(viewModel) }
    val endpoint =
        playEndpoint
            ?: playlistId?.takeUnless { it.startsWith("UC") }?.let { YouTubeWatchEndpoint(playlistId = it.removePrefix("VL")) }
            ?: return null
    return { viewModel.playFromEndpoint(endpoint, title) }
}

/**
 * Spotify-style hover for a Home card: while the pointer is over it, or keyboard focus is on it,
 * the card sits on a faint onSurface tint and [HoverPlayButton] rises onto the artwork. Touch
 * screens never hover, so phones keep the plain card. Returns the card's modifier (it has to come
 * before the card's own focusable to see its focus) and whether the card is active.
 */
@Composable
internal fun rememberCardHover(enabled: Boolean): Pair<Modifier, Boolean> {
    val source = remember { MutableInteractionSource() }
    val hovered by source.collectIsHoveredAsState()
    var focused by remember { mutableStateOf(false) }
    val active = enabled && (hovered || focused)
    val tint by animateColorAsState(
        targetValue = if (active) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.07f) else Color.Transparent,
        label = "cardHoverTint",
    )
    val modifier =
        Modifier
            .onFocusChanged { focused = it.isFocused }
            .hoverable(source)
            .drawBehind { drawRoundRect(color = tint, cornerRadius = CornerRadius(8.dp.toPx())) }
    return modifier to active
}

/** The theme-coloured play button of a hovered Home card, rising onto the artwork's corner. */
@Composable
internal fun BoxScope.HoverPlayButton(
    visible: Boolean,
    onClick: () -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(200),
        label = "hoverPlay",
    )
    Box(
        Modifier
            .align(Alignment.BottomEnd)
            .padding(8.dp)
            .graphicsLayer {
                alpha = progress
                translationY = (1f - progress) * 8.dp.toPx()
            }.size(44.dp)
            .shadow(6.dp, CircleShape)
            .background(MaterialTheme.colorScheme.primary, CircleShape)
            .clickable(enabled = visible, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = SimpIcons.PlayArrow,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(24.dp),
        )
    }
}

internal fun Content.playRadio(viewModel: BaseViewModel) {
    val firstQueue: Track = toTrack()
    viewModel.setQueueData(
        QueueData.Data(
            listTracks = arrayListOf(firstQueue),
            firstPlayedTrack = firstQueue,
            playlistId = "RDAMVM$videoId",
            playlistName = "\"$title\" ${getStringBlocking(Res.string.radio)}",
            playlistType = PlaylistType.RADIO,
            continuation = null,
        ),
    )
    viewModel.loadMediaItem(
        firstQueue,
        Config.SONG_CLICK,
    )
}

@Composable
fun HomeItemContentPlaylist(
    onClick: () -> Unit,
    data: HomeContentType,
    thumbSize: Dp = 160.dp,
    forceDark: Boolean = LocalForceDarkText.current,
    // Fill a grid cell instead of the fixed thumbSize, as Metrolist's browse grid does.
    fillMaxWidth: Boolean = false,
    // Home's hover play button; null leaves the card exactly as it was.
    onPlay: (() -> Unit)? = null,
) {
    val titleColor = if (forceDark) Color.White else MaterialTheme.colorScheme.onSurface
    val textWidth = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(thumbSize)
    val (hoverModifier, hovered) = rememberCardHover(enabled = onPlay != null)
    Box(
        Modifier
            .then(hoverModifier)
            .wrapContentSize()
            .focusable(true)
            .clip(RoundedCornerShape(8.dp))
            .clickable {
                onClick()
            },
    ) {
        Column(
            modifier =
                Modifier
                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 6.dp)
                    // One floor for every home card, so a row mixing them stays one height. 70
                    // covers the tallest text block among them, HomeItemSong's: 69.5dp when the
                    // text engine counts Poppins' line gap (Skia on Desktop does), 65.8dp when not.
                    // A cell-filling card has no such row to match and its artwork is narrower than
                    // thumbSize, so the floor would only add empty space under every tile.
                    .then(if (fillMaxWidth) Modifier else Modifier.heightIn(min = thumbSize + 70.dp)),
        ) {
            val thumb =
                when (data) {
                    is Content -> data.thumbnails.lastOrNull()?.url
                    is com.maxrave.domain.data.model.mood.genre.Content -> data.thumbnail?.lastOrNull()?.url
                    is com.maxrave.domain.data.model.mood.moodmoments.Content -> data.thumbnails?.lastOrNull()?.url
                    is LocalPlaylistEntity -> data.thumbnail
                    is ChartItem -> null
                    is PlaylistsResult -> data.thumbnails.lastOrNull()?.url
                    is AlbumEntity -> data.thumbnails
                    is PlaylistEntity -> data.thumbnails
                    is ResultSingle -> data.thumbnails.lastOrNull()?.url
                    is ResultAlbum -> data.thumbnails.lastOrNull()?.url
                    is ResultPlaylist -> data.thumbnails.lastOrNull()?.url
                    is PodcastsEntity -> data.thumbnail
                    is AlbumsResult -> data.thumbnails.lastOrNull()?.url
                    // A recap owns no artwork; it borrows the month's own top song's, resolved
                    // where the recap itself is (LibraryViewModel.getMonthlyRecaps).
                    // Deliberately null: a recap has no artwork of its own, so it falls to
                    // the title placeholder below — the same tile a local playlist without
                    // a thumbnail gets. Same reason ChartItem above is null.
                    is MonthlyRecapItem -> null
                    else -> null
                }
            Box {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalPlatformContext.current)
                            .data(thumb)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .diskCacheKey(thumb)
                            .crossfade(550)
                            .build(),
                    placeholder =
                        when (data) {
                            is LocalPlaylistEntity -> {
                                painterPlaylistThumbnail(
                                    data.title,
                                    style = typo().bodySmall,
                                    thumbSize * 0.9f to thumbSize * 0.9f,
                                )
                            }

                            is ChartItem -> {
                                painterPlaylistThumbnail(
                                    data.name,
                                    style = typo().bodySmall,
                                    thumbSize * 0.9f to thumbSize * 0.9f,
                                )
                            }

                            // A month whose top song has no artwork still has a name, and the
                            // deterministic title tile reads as a playlist where the grey holder
                            // reads as a failed load.
                            is MonthlyRecapItem -> {
                                painterPlaylistThumbnail(
                                    data.title,
                                    style = typo().bodySmall,
                                    thumbSize * 0.9f to thumbSize * 0.9f,
                                )
                            }

                            else -> {
                                rememberHolderPainter()
                            }
                        },
                    error =
                        when (data) {
                            is LocalPlaylistEntity -> {
                                painterPlaylistThumbnail(
                                    data.title,
                                    style = typo().bodySmall,
                                    thumbSize * 0.9f to thumbSize * 0.9f,
                                )
                            }

                            is ChartItem -> {
                                painterPlaylistThumbnail(
                                    data.name,
                                    style = typo().bodySmall,
                                    thumbSize * 0.9f to thumbSize * 0.9f,
                                )
                            }

                            // A month whose top song has no artwork still has a name, and the
                            // deterministic title tile reads as a playlist where the grey holder
                            // reads as a failed load.
                            is MonthlyRecapItem -> {
                                painterPlaylistThumbnail(
                                    data.title,
                                    style = typo().bodySmall,
                                    thumbSize * 0.9f to thumbSize * 0.9f,
                                )
                            }

                            else -> {
                                rememberHolderPainter()
                            }
                        },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        (if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.size(thumbSize))
                            .aspectRatio(1f)
                            .clip(
                                RoundedCornerShape(10.dp),
                            ),
                )
                if (onPlay != null) HoverPlayButton(visible = hovered, onClick = onPlay)
            }
            Text(
                text =
                    when (data) {
                        is Content -> data.title
                        is com.maxrave.domain.data.model.mood.genre.Content -> data.title.title
                        is com.maxrave.domain.data.model.mood.moodmoments.Content -> data.title
                        is LocalPlaylistEntity -> data.title
                        is ChartItem -> data.name
                        is PlaylistsResult -> data.title
                        is AlbumEntity -> data.title
                        is PlaylistEntity -> data.title
                        is ResultSingle -> data.title
                        is ResultAlbum -> data.title
                        is ResultPlaylist -> data.title
                        is PodcastsEntity -> data.title
                        is AlbumsResult -> data.title
                        is MonthlyRecapItem -> data.title
                        else -> ""
                    },
                style = typo().titleSmall,
                color = titleColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    textWidth
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(top = 8.dp),
            )
            Text(
                text =
                    when (data) {
                        is Content -> {
                            data.description
                                ?: if (data.playlistId == null) {
                                    (
                                        if (!data.artists.isNullOrEmpty()) {
                                            data.artists
                                                .toListName()
                                                .connectArtists()
                                        } else {
                                            stringResource(Res.string.album)
                                        }
                                    )
                                } else {
                                    stringResource(Res.string.playlist)
                                }
                        }

                        is com.maxrave.domain.data.model.mood.genre.Content -> {
                            data.title.subtitle
                        }

                        is com.maxrave.domain.data.model.mood.moodmoments.Content -> {
                            data.subtitle
                        }

                        is LocalPlaylistEntity -> {
                            stringResource(Res.string.you)
                        }

                        is ChartItem -> {
                            stringResource(Res.string.app_name)
                        }

                        is PlaylistsResult -> {
                            data.author
                        }

                        is AlbumEntity -> {
                            data.artistName?.connectArtists() ?: stringResource(Res.string.album)
                        }

                        is PlaylistEntity -> {
                            data.author ?: stringResource(Res.string.playlist)
                        }

                        is ResultSingle -> {
                            data.year
                        }

                        is ResultAlbum -> {
                            data.year
                        }

                        is ResultPlaylist -> {
                            data.author
                        }

                        is PodcastsEntity -> {
                            data.authorName
                        }

                        is MonthlyRecapItem -> {
                            stringResource(Res.string.wrapped_recap_subtitle)
                        }

                        is AlbumsResult -> {
                            data.year
                        }

                        else -> {
                            ""
                        }
                    },
                style = typo().bodySmall,
                minLines = 1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    textWidth
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .basicMarquee(
                            initialDelayMillis = 2000,
                            repeatDelayMillis = 2000,
                            velocity = 25.dp,
                        ),
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeItemSong(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    data: Content,
    // Fill a grid cell instead of the fixed 160dp, as Metrolist's browse grid does.
    fillMaxWidth: Boolean = false,
    // Home's hover play button; null leaves the card exactly as it was.
    onPlay: (() -> Unit)? = null,
) {
    val textWidth = if (fillMaxWidth) Modifier.fillMaxWidth() else Modifier.width(160.dp)
    val (hoverModifier, hovered) = rememberCardHover(enabled = onPlay != null)
    Box(
        modifier =
            Modifier
                .then(hoverModifier)
                .fillMaxSize()
                .focusable(true)
                .clip(RoundedCornerShape(8.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                ),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 6.dp)
                    .heightIn(min = 230.dp),
        ) {
            val thumb =
                data.thumbnails.lastOrNull()?.url?.let {
                    if (it.contains("w120")) {
                        Regex("([wh])120").replace(it, "$1544")
                    } else {
                        it
                    }
                }
            Box(Modifier.align(Alignment.CenterHorizontally)) {
                AsyncImage(
                    model =
                        ImageRequest
                            .Builder(LocalPlatformContext.current)
                            .data(thumb)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .diskCacheKey(thumb)
                            .crossfade(550)
                            .build(),
                    placeholder = rememberHolderPainter(),
                    error = rememberHolderPainter(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .then(if (fillMaxWidth) Modifier.fillMaxWidth().aspectRatio(1f) else Modifier.size(160.dp))
                            .clip(
                                RoundedCornerShape(10.dp),
                            ),
                )
                if (onPlay != null) HoverPlayButton(visible = hovered, onClick = onPlay)
            }
            Text(
                text = data.title,
                style = typo().titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier =
                    textWidth
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .padding(top = 8.dp),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                AnimatedVisibility(visible = data.isExplicit == true) {
                    ExplicitBadge(
                        modifier =
                            Modifier
                                .size(20.dp)
                                .padding(end = 4.dp)
                                .weight(1f),
                    )
                }
                Text(
                    text =
                        listOfNotNull(
                            data.artists
                                .toListName()
                                .connectArtists()
                                .takeIf { it.isNotBlank() },
                            data.album?.name?.takeIf { it.isNotBlank() },
                        ).joinToString(" • "),
                    style = typo().bodySmall,
                    minLines = 1,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier =
                        textWidth
                            .wrapContentHeight(align = Alignment.CenterVertically)
                            .basicMarquee(
                                initialDelayMillis = 2000,
                                repeatDelayMillis = 2000,
                                velocity = 25.dp,
                            ).padding(vertical = 3.dp),
                )
            }
        }
    }
}

/**
 * The red LIVE chip on a broadcast that is on air right now. Sized off the subtitle beside it
 * (bodySmall, 11sp) and kept a step smaller, as on the web — this app's labelSmall is 14sp
 * SemiBold, which made the chip taller than the line it sits on.
 */
@Composable
internal fun LiveBadge(modifier: Modifier = Modifier) {
    Text(
        text = stringResource(Res.string.live_badge),
        style = typo().bodySmall.copy(fontSize = 9.sp, lineHeight = 11.sp, fontWeight = FontWeight.Bold),
        color = Color.White,
        maxLines = 1,
        modifier =
            modifier
                // YouTube's own live red, so the chip reads the same as on the web.
                .background(LiveBadgeRed, RoundedCornerShape(2.dp))
                .padding(horizontal = 3.dp),
    )
}

private val LiveBadgeRed = Color(0xFFCC0000)
