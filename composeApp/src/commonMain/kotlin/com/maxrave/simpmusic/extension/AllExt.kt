package com.maxrave.simpmusic.extension

import androidx.compose.runtime.Composable
import com.maxrave.common.SponsorBlockType
import com.maxrave.domain.data.model.browse.artist.ArtistBrowse
import com.maxrave.domain.data.model.streams.TimeLine
import com.maxrave.domain.extension.now
import com.maxrave.domain.utils.FilterState
import com.maxrave.domain.utils.toTrack
import kotlin.math.roundToLong
import com.maxrave.simpmusic.viewModel.ArtistScreenData
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.custom_order
import simpmusic.composeapp.generated.resources.filler
import simpmusic.composeapp.generated.resources.interaction
import simpmusic.composeapp.generated.resources.intro
import simpmusic.composeapp.generated.resources.music_off_topic
import simpmusic.composeapp.generated.resources.live_badge
import simpmusic.composeapp.generated.resources.na_na
import simpmusic.composeapp.generated.resources.newer_first
import simpmusic.composeapp.generated.resources.older_first
import simpmusic.composeapp.generated.resources.outro
import simpmusic.composeapp.generated.resources.poi_highlight
import simpmusic.composeapp.generated.resources.preview
import simpmusic.composeapp.generated.resources.self_promotion
import simpmusic.composeapp.generated.resources.sponsor
import simpmusic.composeapp.generated.resources.title
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Whether a release from an artist's discography page is worth announcing. An unseen id is not
 * enough on its own, because YouTube re-keys old releases and pages them in and out, so only
 * releases dated this year count — plus last year's during January, so one that came out in
 * the last days of December is not lost.
 */
fun isNewRelease(
    browseId: String,
    year: String,
    known: Set<String>,
    today: LocalDate = now().date,
): Boolean {
    if (browseId in known) return false
    val released = year.toIntOrNull() ?: return false
    return released == today.year || (today.month == Month.JANUARY && released == today.year - 1)
}

/**
 * The elapsed-time label under a seek bar sitting at [progress] (0..1) of the track. Blank for a live
 * broadcast: where playback sits in the broadcast's seek window means nothing to a listener.
 */
@Composable
fun TimeLine.elapsedLabel(progress: Float): String = if (isLive) "" else formatDuration((total * progress).roundToLong())

/** The track's length under a seek bar, or LIVE for a live broadcast, which has none. */
@Composable
fun TimeLine.lengthLabel(): String = if (isLive) stringResource(Res.string.live_badge) else formatDuration(total)

@Composable
fun formatDuration(duration: Long): String {
    if (duration < 0L) return stringResource(Res.string.na_na)
    val minutes: Long = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds: Long = (
        TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
            minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES)
    )
    return String.format(Locale.ENGLISH, "%02d:%02d", minutes, seconds)
}

fun InputStream.zipInputStream(): ZipInputStream = ZipInputStream(this)

fun OutputStream.zipOutputStream(): ZipOutputStream = ZipOutputStream(this)

fun Long?.bytesToMB(): Long {
    val mbInBytes = 1024 * 1024
    return this?.div(mbInBytes) ?: 0L
}

fun getSizeOfFile(dir: File): Long {
    var dirSize: Long = 0
    if (!dir.listFiles().isNullOrEmpty()) {
        for (f in dir.listFiles()!!) {
            dirSize += f.length()
            if (f.isDirectory) {
                dirSize += getSizeOfFile(f)
            }
        }
    }
    return dirSize
}

fun ArtistBrowse.toArtistScreenData(): ArtistScreenData =
    ArtistScreenData(
        title = this.name,
        imageUrl = this.thumbnails?.lastOrNull()?.url,
        subscribers = this.subscribers,
        playCount = this.views,
        isChannel = this.songs == null,
        channelId = this.channelId,
        radioParam = this.radioId,
        shuffleParam = this.shuffleId,
        description = this.description,
        listSongParam = this.songs?.browseId,
        popularSongs = this.songs?.results?.map { it.toTrack() } ?: emptyList(),
        singles = this.singles,
        albums = this.albums,
        video =
            this.video?.let { video ->
                ArtistBrowse.Videos(video.map { it.toTrack() }, this.videoList)
            },
        related = this.related,
        featuredOn = this.featuredOn ?: emptyList(),
    )

fun String.isTwoLetterCode(): Boolean {
    val regex = "^[A-Za-z]{2}$".toRegex()
    return regex.matches(this)
}

fun FilterState.displayNameRes(): StringResource =
    when (this) {
        FilterState.NewerFirst -> Res.string.newer_first
        FilterState.OlderFirst -> Res.string.older_first
        FilterState.Title -> Res.string.title
        FilterState.CustomOrder -> Res.string.custom_order
    }

@Composable
fun SponsorBlockType.displayString(): String = stringResource(displayRes())

/** The resource behind [displayString], for callers outside composition (the skip toast). */
fun SponsorBlockType.displayRes(): StringResource =
    when (this) {
        SponsorBlockType.FILLER -> Res.string.filler
        SponsorBlockType.INTERACTION -> Res.string.interaction
        SponsorBlockType.INTRO -> Res.string.intro
        SponsorBlockType.MUSIC_OFF_TOPIC -> Res.string.music_off_topic
        SponsorBlockType.OUTRO -> Res.string.outro
        SponsorBlockType.POI_HIGHLIGHT -> Res.string.poi_highlight
        SponsorBlockType.PREVIEW -> Res.string.preview
        SponsorBlockType.SELF_PROMOTION -> Res.string.self_promotion
        SponsorBlockType.SPONSOR -> Res.string.sponsor
    }
