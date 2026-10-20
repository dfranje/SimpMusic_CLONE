package com.maxrave.simpmusic.ui.screen.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.eygraber.uri.toKmpUri
import com.maxrave.common.LIMIT_CACHE_SIZE
import com.maxrave.common.QUALITY
import com.maxrave.common.SUPPORTED_LANGUAGE
import com.maxrave.common.SUPPORTED_LOCATION
import com.maxrave.common.SponsorBlockType
import com.maxrave.domain.extension.now
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.DataStoreManager.Values.TRUE
import com.maxrave.domain.repository.ImportProgress
import com.maxrave.domain.utils.LocalResource
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.expect.ui.canFollowXyzLauncher
import com.maxrave.simpmusic.expect.ui.filePickerResult
import com.maxrave.simpmusic.expect.ui.fileSaverResult
import com.maxrave.simpmusic.expect.ui.openEqResult
import com.maxrave.simpmusic.extension.barBlurStyle
import com.maxrave.simpmusic.extension.bytesToMB
import com.maxrave.simpmusic.extension.displayString
import com.maxrave.simpmusic.extension.isTwoLetterCode
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.ActionButton
import com.maxrave.simpmusic.ui.component.AmbientThemeGlow
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.LoadingDialog
import com.maxrave.simpmusic.ui.component.RippleIconButton
import com.maxrave.simpmusic.ui.component.SettingGroup
import com.maxrave.simpmusic.ui.component.SettingItem
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.Error
import com.maxrave.simpmusic.ui.icon.PeopleAlt
import com.maxrave.simpmusic.ui.icon.PlaylistAdd
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.home.AppLogDestination
import com.maxrave.simpmusic.ui.navigation.destination.login.LastfmLoginDestination
import com.maxrave.simpmusic.ui.navigation.destination.login.LoginDestination
import com.maxrave.simpmusic.ui.theme.LocalXyzPalette
import com.maxrave.simpmusic.ui.theme.XYZ_ACCENT_FOLLOW
import com.maxrave.simpmusic.ui.theme.XyzAccents
import com.maxrave.simpmusic.ui.theme.md_theme_dark_primary
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.xyzAccentIndex
import com.maxrave.simpmusic.ui.theme.xyzAccentSettingOrDefault
import com.maxrave.simpmusic.utils.VersionManager
import com.maxrave.simpmusic.viewModel.ImportViewModel
import com.maxrave.simpmusic.viewModel.SettingAlertState
import com.maxrave.simpmusic.viewModel.SettingBasicAlertState
import com.maxrave.simpmusic.viewModel.SettingsViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.blur.hazeBlur
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.about_us
import simpmusic.composeapp.generated.resources.add_an_account
import simpmusic.composeapp.generated.resources.app_log
import simpmusic.composeapp.generated.resources.app_log_description
import simpmusic.composeapp.generated.resources.app_name
import simpmusic.composeapp.generated.resources.developer_option
import simpmusic.composeapp.generated.resources.audio
import simpmusic.composeapp.generated.resources.backup
import simpmusic.composeapp.generated.resources.balance_media_loudness
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.categories_sponsor_block
import simpmusic.composeapp.generated.resources.change
import simpmusic.composeapp.generated.resources.change_language_warning
import simpmusic.composeapp.generated.resources.clear
import simpmusic.composeapp.generated.resources.clear_canvas_cache
import simpmusic.composeapp.generated.resources.clear_downloaded_cache
import simpmusic.composeapp.generated.resources.clear_listening_history
import simpmusic.composeapp.generated.resources.clear_listening_history_confirm
import simpmusic.composeapp.generated.resources.clear_listening_history_description
import simpmusic.composeapp.generated.resources.clear_player_cache
import simpmusic.composeapp.generated.resources.clear_thumbnail_cache
import simpmusic.composeapp.generated.resources.content
import simpmusic.composeapp.generated.resources.content_country
import simpmusic.composeapp.generated.resources.crossfade
import simpmusic.composeapp.generated.resources.crossfade_auto
import simpmusic.composeapp.generated.resources.crossfade_description
import simpmusic.composeapp.generated.resources.crossfade_dj_mode
import simpmusic.composeapp.generated.resources.crossfade_dj_mode_description
import simpmusic.composeapp.generated.resources.crossfade_duration
import simpmusic.composeapp.generated.resources.crossfade_skip_album
import simpmusic.composeapp.generated.resources.crossfade_skip_album_description
import simpmusic.composeapp.generated.resources.database
import simpmusic.composeapp.generated.resources.download_quality
import simpmusic.composeapp.generated.resources.downloaded_cache
import simpmusic.composeapp.generated.resources.enable_scrobbling
import simpmusic.composeapp.generated.resources.enable_sponsor_block
import simpmusic.composeapp.generated.resources.equalizer
import simpmusic.composeapp.generated.resources.equalizer_description
import simpmusic.composeapp.generated.resources.equalizer_type
import simpmusic.composeapp.generated.resources.equalizer_type_built_in
import simpmusic.composeapp.generated.resources.equalizer_type_system
import simpmusic.composeapp.generated.resources.free_space
import simpmusic.composeapp.generated.resources.guest
import simpmusic.composeapp.generated.resources.import_data
import simpmusic.composeapp.generated.resources.import_failed
import simpmusic.composeapp.generated.resources.import_progress_songs
import simpmusic.composeapp.generated.resources.import_reading_file
import simpmusic.composeapp.generated.resources.import_result
import simpmusic.composeapp.generated.resources.import_result_skipped
import simpmusic.composeapp.generated.resources.intro_login_to_lastfm
import simpmusic.composeapp.generated.resources.invalid_language_code
import simpmusic.composeapp.generated.resources.kill_service_on_exit
import simpmusic.composeapp.generated.resources.kill_service_on_exit_description
import simpmusic.composeapp.generated.resources.language
import simpmusic.composeapp.generated.resources.lastfm_integration
import simpmusic.composeapp.generated.resources.limit_player_cache
import simpmusic.composeapp.generated.resources.listening_history
import simpmusic.composeapp.generated.resources.local_tracking_description
import simpmusic.composeapp.generated.resources.local_tracking_title
import simpmusic.composeapp.generated.resources.log_in_to_lastfm
import simpmusic.composeapp.generated.resources.log_out
import simpmusic.composeapp.generated.resources.log_out_from_lastfm
import simpmusic.composeapp.generated.resources.log_out_warning
import simpmusic.composeapp.generated.resources.logged_in_as
import simpmusic.composeapp.generated.resources.manage_your_youtube_accounts
import simpmusic.composeapp.generated.resources.no_account
import simpmusic.composeapp.generated.resources.normalize_volume
import simpmusic.composeapp.generated.resources.not_available_while_casting
import simpmusic.composeapp.generated.resources.ok
import simpmusic.composeapp.generated.resources.open_system_equalizer
import simpmusic.composeapp.generated.resources.original_audio
import simpmusic.composeapp.generated.resources.other_app
import simpmusic.composeapp.generated.resources.play_explicit_content
import simpmusic.composeapp.generated.resources.play_explicit_content_description
import simpmusic.composeapp.generated.resources.playback
import simpmusic.composeapp.generated.resources.player_cache
import simpmusic.composeapp.generated.resources.preferred_audio_language
import simpmusic.composeapp.generated.resources.preferred_audio_language_message
import simpmusic.composeapp.generated.resources.quality
import simpmusic.composeapp.generated.resources.radio_audio_only
import simpmusic.composeapp.generated.resources.radio_audio_only_description
import simpmusic.composeapp.generated.resources.save
import simpmusic.composeapp.generated.resources.save_playback_state
import simpmusic.composeapp.generated.resources.save_shuffle_and_repeat_mode
import simpmusic.composeapp.generated.resources.scrobbling_info
import simpmusic.composeapp.generated.resources.settings
import simpmusic.composeapp.generated.resources.signed_in
import simpmusic.composeapp.generated.resources.skip_no_music_part
import simpmusic.composeapp.generated.resources.skip_silent
import simpmusic.composeapp.generated.resources.skip_sponsor_part_of_video
import simpmusic.composeapp.generated.resources.sponsorBlock
import simpmusic.composeapp.generated.resources.sponsor_block_intro
import simpmusic.composeapp.generated.resources.spotify_canvas_cache
import simpmusic.composeapp.generated.resources.storage
import simpmusic.composeapp.generated.resources.theme
import simpmusic.composeapp.generated.resources.theme_follows_xyz_launcher
import simpmusic.composeapp.generated.resources.xyz_accent
import simpmusic.composeapp.generated.resources.xyz_accent_follow
import simpmusic.composeapp.generated.resources.xyz_launcher_not_found
import simpmusic.composeapp.generated.resources.theme_mode_dark
import simpmusic.composeapp.generated.resources.theme_mode_light
import simpmusic.composeapp.generated.resources.theme_mode_system
import simpmusic.composeapp.generated.resources.thumbnail_cache
import simpmusic.composeapp.generated.resources.use_your_system_equalizer
import simpmusic.composeapp.generated.resources.user_interface
import simpmusic.composeapp.generated.resources.version
import simpmusic.composeapp.generated.resources.version_format
import simpmusic.composeapp.generated.resources.warning
import simpmusic.composeapp.generated.resources.what_segments_will_be_skipped
import simpmusic.composeapp.generated.resources.youtube_account

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalCoilApi::class,
    FormatStringsInDatetimeFormats::class,
)
@Composable
fun SettingScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    viewModel: SettingsViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val platformContext = LocalPlatformContext.current
    val localDensity = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    var width by rememberSaveable { mutableIntStateOf(0) }

    // Backup and restore
    val formatter =
        LocalDateTime.Format {
            byUnicodePattern("yyyyMMddHHmmss")
        }
    val appName = stringResource(Res.string.app_name)

        fileSaverResult(
            "${appName}_${
                now().format(
                    formatter,
                )
            }.backup",
            "application/octet-stream",
        ) { uri ->
            uri?.let {
                viewModel.backup(it.toKmpUri())
            }
        }

        filePickerResult { uri ->
            uri?.let {
                viewModel.restore(it.toKmpUri())
            }
        }

    // Import playlists converted on the web. filePickerResult offers every file type, because a
    // converted .json arrives with whatever MIME its source assigned it, and an application/json
    // filter would hide it on some hosts.
    val importViewModel: ImportViewModel = koinViewModel()
    val importState by importViewModel.importState.collectAsStateWithLifecycle()
        filePickerResult { uri ->
            uri?.let {
                importViewModel.import(it.toKmpUri())
            }
        }

    // Open equalizer
    val resultLauncher = openEqResult(viewModel.getAudioSessionId())

    val language by viewModel.language.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val quality by viewModel.quality.collectAsStateWithLifecycle()
    val downloadQuality by viewModel.downloadQuality.collectAsStateWithLifecycle()
    val localTrackingEnabled by viewModel.localTrackingEnabled.collectAsStateWithLifecycle(initialValue = false)
    val combineLocalAndYouTubeLiked by viewModel.combineLocalAndYouTubeLiked.collectAsStateWithLifecycle()
    val radioAudioOnly by remember { viewModel.radioAudioOnly.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val normalizeVolume by remember { viewModel.normalizeVolume.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val skipSilent by remember { viewModel.skipSilent.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val savePlaybackState by remember { viewModel.savedPlaybackState.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val killServiceOnExit by remember { viewModel.killServiceOnExit.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = true)
    val preferredAudioLanguage by viewModel.preferredAudioLanguage.collectAsStateWithLifecycle()
    val enableSponsorBlock by remember { viewModel.sponsorBlockEnabled.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val skipSegments by viewModel.sponsorBlockCategories.collectAsStateWithLifecycle()
    val playerCache by viewModel.cacheSize.collectAsStateWithLifecycle()
    val downloadedCache by viewModel.downloadedCacheSize.collectAsStateWithLifecycle()
    val thumbnailCache by viewModel.thumbCacheSize.collectAsStateWithLifecycle()
    val canvasCache by viewModel.canvasCacheSize.collectAsStateWithLifecycle()
    val limitPlayerCache by viewModel.playerCacheLimit.collectAsStateWithLifecycle()
    val fraction by viewModel.fraction.collectAsStateWithLifecycle()
    val explicitContentEnabled by viewModel.explicitContentEnabled.collectAsStateWithLifecycle()
    val themeMode by sharedViewModel.getThemeMode().collectAsStateWithLifecycle(DataStoreManager.THEME_MODE_DARK)
    val storedXyzAccent by sharedViewModel.getXyzAccent().collectAsStateWithLifecycle(null)
    val xyzAccent = xyzAccentSettingOrDefault(storedXyzAccent, canFollowXyzLauncher())
    // Provided by AppTheme: tells whether the launcher actually answered, i.e. whether day/night is its call.
    val xyzPalette = LocalXyzPalette.current
    val followsLauncherDayNight =
        xyzAccent == XYZ_ACCENT_FOLLOW && xyzPalette?.fromLauncher == true
    val equalizerEnabled by viewModel.equalizerEnabled.collectAsStateWithLifecycle()
    val equalizerType by viewModel.equalizerType.collectAsStateWithLifecycle()
    val lastfmLoggedIn by viewModel.lastfmLoggedIn.collectAsStateWithLifecycle()
    val lastfmUsername by viewModel.lastfmUsername.collectAsStateWithLifecycle()
    val lastfmScrobbleEnabled by viewModel.lastfmScrobbleEnabled.collectAsStateWithLifecycle()

    val crossfadeEnabled by viewModel.crossfadeEnabled.collectAsStateWithLifecycle()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsStateWithLifecycle()
    val crossfadeDjMode by viewModel.crossfadeDjMode.collectAsStateWithLifecycle()
    val crossfadeSkipAlbum by viewModel.crossfadeSkipAlbum.collectAsStateWithLifecycle()
    val castState by viewModel.castState.collectAsStateWithLifecycle()


    val hazeState =
        rememberHazeState()

    var showYouTubeAccountDialog by rememberSaveable {
        mutableStateOf(false)
    }

    LaunchedEffect(true) {
        viewModel.getAllGoogleAccount()
    }

    LaunchedEffect(true) {
        viewModel.getData()
        viewModel.getThumbCacheSize(platformContext)
    }

    val settingListState = rememberLazyListState()
    // The toolbar is part of the list and scrolls away with it; a pinned, frosted copy takes over
    // once the list has moved under it — Playlist's and Artist's way.
    val showPinnedBar by remember {
        derivedStateOf {
            settingListState.firstVisibleItemIndex > 0 ||
                settingListState.firstVisibleItemScrollOffset > 0
        }
    }
    // The list's side margin, which the in-list toolbar stretches back across.
    val listSidePadding = 16.dp
    // Home-family ambient ground, and like Home's it SCROLLS AWAY with the content instead of
    // hanging off the ceiling. Still a sibling (so it sits behind the floating bar), but its draw
    // rides the list: exact tracking while item 0 is on screen, parked off-screen after. Item 0 is
    // taller than the glow, so the glow has fully left before the branch ever switches — no jump.
    // graphicsLayer reads the state in the DRAW phase, so scrolling redraws without recomposing.
    // Tinted with the app's own primary rather than the song playing: this page is about the app.
    AmbientThemeGlow(
        tint = MaterialTheme.colorScheme.primary,
        modifier =
            Modifier.graphicsLayer {
                translationY =
                    if (settingListState.firstVisibleItemIndex == 0) {
                        -settingListState.firstVisibleItemScrollOffset.toFloat()
                    } else {
                        -size.height
                    }
            },
    )
    LazyColumn(
        state = settingListState,
        contentPadding = innerPadding,
        modifier =
            Modifier
                .padding(horizontal = listSidePadding)
                .hazeSource(hazeState),
    ) {
        item(key = "user_interface") {
            Column {
                // Was its own item. Folded in so item 0 is taller than the glow — the glow's
                // translation tracks item 0's offset exactly and parks once it scrolls past, and a
                // 64dp item 0 would have switched branches while the glow was still half-visible.
                // The toolbar's in-list copy, stretched across the list's side margin so it sits exactly
                // where the pinned copy appears; the list's top padding already clears the status bar.
                SettingTopBar(
                    onBack = { navController.navigateUp() },
                    windowInsets = WindowInsets(0),
                    modifier =
                        Modifier.layout { measurable, constraints ->
                            val bleed = listSidePadding.roundToPx()
                            val width = constraints.maxWidth + 2 * bleed
                            val placeable = measurable.measure(constraints.copy(minWidth = width, maxWidth = width))
                            layout(constraints.maxWidth, placeable.height) { placeable.place(-bleed, 0) }
                        },
                )
                SettingGroup(title = stringResource(Res.string.user_interface)) {
                    val themeModeLabels =
                        listOf(
                            DataStoreManager.THEME_MODE_SYSTEM to stringResource(Res.string.theme_mode_system),
                            DataStoreManager.THEME_MODE_DARK to stringResource(Res.string.theme_mode_dark),
                            DataStoreManager.THEME_MODE_LIGHT to stringResource(Res.string.theme_mode_light),
                        )
                    SettingItem(
                        title = stringResource(Res.string.theme),
                        subtitle =
                            if (followsLauncherDayNight) {
                                stringResource(Res.string.theme_follows_xyz_launcher)
                            } else {
                                themeModeLabels.firstOrNull { it.first == themeMode }?.second ?: ""
                            },
                        // The launcher switches day and night itself (its own setting, or sunset); the
                        // stored mode comes back the moment the XYZ look stops following it.
                        isEnable = !followsLauncherDayNight,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.theme) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = themeModeLabels.map { (it.first == themeMode) to it.second },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            themeModeLabels.firstOrNull { it.second == selected }?.first?.let {
                                                sharedViewModel.setThemeMode(it)
                                            }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    val accentLabels =
                        buildList {
                            if (canFollowXyzLauncher()) add(XYZ_ACCENT_FOLLOW to stringResource(Res.string.xyz_accent_follow))
                            XyzAccents.forEachIndexed { index, accent -> add(index.toString() to stringResource(accent.name)) }
                        }
                    val fallbackAccentName = stringResource(XyzAccents[xyzAccentIndex(xyzAccent)].name)
                    SettingItem(
                        title = stringResource(Res.string.xyz_accent),
                        subtitle =
                            when {
                                xyzAccent != XYZ_ACCENT_FOLLOW -> accentLabels.firstOrNull { it.first == xyzAccent }?.second ?: ""
                                xyzPalette != null && xyzPalette.fromLauncher ->
                                    stringResource(Res.string.xyz_accent_follow) + " · " +
                                        stringResource(XyzAccents[xyzPalette.accentIndex].name)
                                else -> stringResource(Res.string.xyz_launcher_not_found, fallbackAccentName)
                            },
                        smallSubtitle = true,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.xyz_accent) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = accentLabels.map { (it.first == xyzAccent) to it.second },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            accentLabels.firstOrNull { it.second == selected }?.first?.let {
                                                sharedViewModel.setXyzAccent(it)
                                            }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                
                }
            }
        }
        item(key = "content") {
            SettingGroup(title = stringResource(Res.string.content)) {
                SettingItem(
                    title = stringResource(Res.string.youtube_account),
                    subtitle = stringResource(Res.string.manage_your_youtube_accounts),
                    onClick = {
                        viewModel.getAllGoogleAccount()
                        showYouTubeAccountDialog = true
                    },
                )
                SettingItem(
                    title = stringResource(Res.string.language),
                    subtitle = SUPPORTED_LANGUAGE.getLanguageFromCode(language ?: "en-US"),
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.language) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            SUPPORTED_LANGUAGE.items.map {
                                                (it.toString() == SUPPORTED_LANGUAGE.getLanguageFromCode(language ?: "en-US")) to it.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        val code = SUPPORTED_LANGUAGE.getCodeFromLanguage(state.selectOne?.getSelected() ?: "English")
                                        viewModel.setBasicAlertData(
                                            SettingBasicAlertState(
                                                title = runBlocking { getString(Res.string.warning) },
                                                message = runBlocking { getString(Res.string.change_language_warning) },
                                                confirm =
                                                    runBlocking { getString(Res.string.change) } to {
                                                        sharedViewModel.activityRecreate()
                                                        viewModel.setBasicAlertData(null)
                                                        viewModel.changeLanguage(code)
                                                    },
                                                dismiss = runBlocking { getString(Res.string.cancel) },
                                            ),
                                        )
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(Res.string.content_country),
                    subtitle = location ?: "",
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.content_country) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            SUPPORTED_LOCATION.items.map { item ->
                                                (item.toString() == location) to item.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        viewModel.changeLocation(
                                            state.selectOne?.getSelected() ?: "US",
                                        )
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(Res.string.preferred_audio_language),
                    subtitle = preferredAudioLanguage.ifEmpty { stringResource(Res.string.original_audio) },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.preferred_audio_language) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(Res.string.preferred_audio_language) },
                                        value = preferredAudioLanguage,
                                        // Empty is valid here: it means "original audio".
                                        verifyCodeBlock = {
                                            (it.isEmpty() || it.isTwoLetterCode()) to
                                                runBlocking { getString(Res.string.invalid_language_code) }
                                        },
                                    ),
                                message = runBlocking { getString(Res.string.preferred_audio_language_message) },
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        viewModel.setPreferredAudioLanguage(state.textField?.value ?: "")
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(Res.string.quality),
                    subtitle = quality ?: "",
                    smallSubtitle = true,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.quality) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            QUALITY.items.map { item ->
                                                (item.toString() == quality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        viewModel.changeQuality(state.selectOne?.getSelected())
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(Res.string.download_quality),
                    subtitle = downloadQuality ?: "",
                    smallSubtitle = true,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.download_quality) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            QUALITY.items.map { item ->
                                                (item.toString() == downloadQuality) to item.toString()
                                            },
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        state.selectOne?.getSelected()?.let { viewModel.setDownloadQuality(it) }
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                )
                SettingItem(
                    title = stringResource(Res.string.radio_audio_only),
                    subtitle = stringResource(Res.string.radio_audio_only_description),
                    smallSubtitle = true,
                    switch = (radioAudioOnly to { viewModel.setRadioAudioOnly(it) }),
                )
                SettingItem(
                    title = stringResource(Res.string.play_explicit_content),
                    subtitle = stringResource(Res.string.play_explicit_content_description),
                    switch = (explicitContentEnabled to { viewModel.setExplicitContentEnabled(it) }),
                )
                /*
                SettingItem(
                    title = stringResource(Res.string.combine_local_and_youtube_liked_songs),
                    subtitle = stringResource(Res.string.combine_local_and_youtube_liked_songs_description),
                    switch = (combineLocalAndYouTubeLiked to { viewModel.setCombineLocalAndYouTubeLiked(it) })
                )
                 */
            }
        }
        if (getPlatform() == Platform.Android) {
            item(key = "audio") {
                SettingGroup(title = stringResource(Res.string.audio)) {
                    SettingItem(
                        title = stringResource(Res.string.normalize_volume),
                        subtitle = stringResource(Res.string.balance_media_loudness),
                        switch = (normalizeVolume to { viewModel.setNormalizeVolume(it) }),
                    )
                    SettingItem(
                        title = stringResource(Res.string.skip_silent),
                        subtitle = stringResource(Res.string.skip_no_music_part),
                        switch = (skipSilent to { viewModel.setSkipSilent(it) }),
                    )
                    val equalizerTypeLabels =
                        listOf(
                            DataStoreManager.EQUALIZER_TYPE_BUILT_IN to stringResource(Res.string.equalizer_type_built_in),
                            DataStoreManager.EQUALIZER_TYPE_SYSTEM to stringResource(Res.string.equalizer_type_system),
                        )
                    SettingItem(
                        title = stringResource(Res.string.equalizer_type),
                        subtitle = equalizerTypeLabels.firstOrNull { it.first == equalizerType }?.second ?: "",
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.equalizer_type) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = equalizerTypeLabels.map { (it.first == equalizerType) to it.second },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            equalizerTypeLabels.firstOrNull { it.second == selected }?.first?.let {
                                                viewModel.setEqualizerType(it)
                                            }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    AnimatedVisibility(visible = equalizerType == DataStoreManager.EQUALIZER_TYPE_SYSTEM) {
                        SettingItem(
                            title = stringResource(Res.string.open_system_equalizer),
                            subtitle =
                                if (castState.isRemote) {
                                    stringResource(Res.string.not_available_while_casting)
                                } else {
                                    stringResource(Res.string.use_your_system_equalizer)
                                },
                            isEnable = !castState.isRemote,
                            onClick = {
                                coroutineScope.launch {
                                    resultLauncher.launch()
                                }
                            },
                        )
                    }
                }
            }
        }
        item(key = "playback") {
            SettingGroup(title = stringResource(Res.string.playback)) {
                // Under Playback rather than Audio because that whole group sits inside an
                // Android-only branch — "Open system equalizer" is an Android feature — and this
                // one is on both platforms: mpv's `af` chain on Desktop, an AudioProcessor in the
                // Media3 sink on Android, driven from the same stored curve.
                AnimatedVisibility(visible = getPlatform() != Platform.Android || equalizerType != DataStoreManager.EQUALIZER_TYPE_SYSTEM) {
                    Column {
                        SettingItem(
                            title = stringResource(Res.string.equalizer),
                            subtitle = stringResource(Res.string.equalizer_description),
                            smallSubtitle = true,
                            switch = (equalizerEnabled to { viewModel.setEqualizerEnabled(it) }),
                        )
                        // Only while on. A curve that visibly does nothing is worse than no curve —
                        // and the stored bands survive the switch, so turning it back on returns to
                        // the shape the user built rather than to flat.
                        AnimatedVisibility(visible = equalizerEnabled) {
                            EqualizerSection()
                        }
                    }
                }
                SettingItem(
                    title = stringResource(Res.string.save_playback_state),
                    subtitle = stringResource(Res.string.save_shuffle_and_repeat_mode),
                    switch = (savePlaybackState to { viewModel.setSavedPlaybackState(it) }),
                )
                if (getPlatform() == Platform.Android) {
                    SettingItem(
                        title = stringResource(Res.string.kill_service_on_exit),
                        subtitle = stringResource(Res.string.kill_service_on_exit_description),
                        switch = (killServiceOnExit to { viewModel.setKillServiceOnExit(it) }),
                    )
                }
                // Crossfade (all platforms)
                SettingItem(
                    title = stringResource(Res.string.crossfade),
                    subtitle =
                        if (castState.isRemote) {
                            stringResource(Res.string.not_available_while_casting)
                        } else {
                            stringResource(Res.string.crossfade_description)
                        },
                    smallSubtitle = true,
                    switch = (crossfadeEnabled to { viewModel.setCrossfadeEnabled(it) }),
                    isEnable = !castState.isRemote,
                )
                AnimatedVisibility(visible = crossfadeEnabled) {
                    Column {
                        SettingItem(
                            title = stringResource(Res.string.crossfade_duration),
                            subtitle =
                                if (castState.isRemote) {
                                    stringResource(Res.string.not_available_while_casting)
                                } else if (crossfadeDuration == DataStoreManager.CROSSFADE_DURATION_AUTO) {
                                    stringResource(Res.string.crossfade_auto)
                                } else {
                                    "${crossfadeDuration / 1000}s"
                                },
                            isEnable = !castState.isRemote,
                            onClick = {
                                viewModel.setAlertData(
                                    SettingAlertState(
                                        title = runBlocking { getString(Res.string.crossfade_duration) },
                                        selectOne =
                                            SettingAlertState.SelectData(
                                                listSelect =
                                                    listOf(
                                                        (crossfadeDuration == DataStoreManager.CROSSFADE_DURATION_AUTO) to
                                                            runBlocking { getString(Res.string.crossfade_auto) },
                                                        (crossfadeDuration == 1000) to "1s",
                                                        (crossfadeDuration == 2000) to "2s",
                                                        (crossfadeDuration == 3000) to "3s",
                                                        (crossfadeDuration == 5000) to "5s",
                                                        (crossfadeDuration == 8000) to "8s",
                                                        (crossfadeDuration == 10000) to "10s",
                                                        (crossfadeDuration == 12000) to "12s",
                                                        (crossfadeDuration == 15000) to "15s",
                                                        (crossfadeDuration == 20000) to "20s",
                                                        (crossfadeDuration == 30000) to "30s",
                                                    ),
                                            ),
                                        confirm =
                                            runBlocking { getString(Res.string.change) } to { state ->
                                                val duration =
                                                    when (state.selectOne?.getSelected()) {
                                                        runBlocking {
                                                            getString(
                                                                Res.string.crossfade_auto,
                                                            )
                                                        },
                                                        -> DataStoreManager.CROSSFADE_DURATION_AUTO
                                                        "1s" -> 1000
                                                        "2s" -> 2000
                                                        "3s" -> 3000
                                                        "5s" -> 5000
                                                        "8s" -> 8000
                                                        "10s" -> 10000
                                                        "12s" -> 12000
                                                        "15s" -> 15000
                                                        "20s" -> 20000
                                                        "30s" -> 30000
                                                        else -> 5000
                                                    }
                                                viewModel.setCrossfadeDuration(duration)
                                            },
                                        dismiss = runBlocking { getString(Res.string.cancel) },
                                    ),
                                )
                            },
                        )
//                        if (getPlatform() == Platform.Android) {
                        SettingItem(
                            title = stringResource(Res.string.crossfade_dj_mode),
                            subtitle =
                                if (castState.isRemote) {
                                    stringResource(Res.string.not_available_while_casting)
                                } else {
                                    stringResource(Res.string.crossfade_dj_mode_description)
                                },
                            smallSubtitle = true,
                            switch = ((crossfadeDjMode) to { viewModel.setCrossfadeDjMode(it) }),
                            isEnable = !castState.isRemote,
                        )
                        SettingItem(
                            title = stringResource(Res.string.crossfade_skip_album),
                            subtitle =
                                if (castState.isRemote) {
                                    stringResource(Res.string.not_available_while_casting)
                                } else {
                                    stringResource(Res.string.crossfade_skip_album_description)
                                },
                            smallSubtitle = true,
                            switch = ((crossfadeSkipAlbum) to { viewModel.setCrossfadeSkipAlbum(it) }),
                            isEnable = !castState.isRemote,
                        )
//                        }
                    }
                }
            }
        }
        // Deliberately not part of "storage" further down, which is Android-only: tracking and the
        // rows it leaves behind exist on Desktop just the same. The switch that produces the history
        // and the button that erases it belong together.
        item(key = "listening_history") {
            SettingGroup(title = stringResource(Res.string.listening_history)) {
                SettingItem(
                    title = stringResource(Res.string.local_tracking_title),
                    subtitle = stringResource(Res.string.local_tracking_description),
                    switch = (localTrackingEnabled to { viewModel.setLocalTrackingEnabled(it) }),
                )
                SettingItem(
                    title = stringResource(Res.string.clear_listening_history),
                    subtitle = stringResource(Res.string.clear_listening_history_description),
                    onClick = {
                        viewModel.setBasicAlertData(
                            SettingBasicAlertState(
                                title = runBlocking { getString(Res.string.clear_listening_history) },
                                message = runBlocking { getString(Res.string.clear_listening_history_confirm) },
                                confirm =
                                    runBlocking { getString(Res.string.clear) } to {
                                        viewModel.clearListeningHistory()
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                )
            }
        }
        // Hidden entirely when the build carries no Last.fm credentials — a FOSS build, or a full
        // build whose local.properties has no key.
        if (viewModel.lastfmAvailable) {
            item(key = "lastfm") {
                SettingGroup(title = stringResource(Res.string.lastfm_integration)) {
                    SettingItem(
                        title =
                            if (lastfmLoggedIn) {
                                stringResource(Res.string.log_out_from_lastfm)
                            } else {
                                stringResource(Res.string.log_in_to_lastfm)
                            },
                        subtitle =
                            if (lastfmLoggedIn) {
                                stringResource(Res.string.logged_in_as, lastfmUsername)
                            } else {
                                stringResource(Res.string.intro_login_to_lastfm)
                            },
                        onClick = {
                            if (lastfmLoggedIn) {
                                viewModel.confirmLogOut(
                                    confirmLabel = runBlocking { getString(Res.string.log_out_from_lastfm) },
                                ) { viewModel.logOutLastfm() }
                            } else {
                                navController.navigate(LastfmLoginDestination)
                            }
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.enable_scrobbling),
                        subtitle = stringResource(Res.string.scrobbling_info),
                        switch = (lastfmScrobbleEnabled to { viewModel.setLastfmScrobbleEnabled(it) }),
                        isEnable = lastfmLoggedIn,
                    )
                }
            }
        }
        item(key = "sponsor_block") {
            SettingGroup(
                title = stringResource(Res.string.sponsorBlock),
                // The project's note sits under the card, where a grouped list keeps its explanations.
                footer = {
                    val beforeUrl = stringResource(Res.string.sponsor_block_intro).substringBefore("https://sponsor.ajay.app/")
                    val afterUrl = stringResource(Res.string.sponsor_block_intro).substringAfter("https://sponsor.ajay.app/")
                    Text(
                        buildAnnotatedString {
                            append(beforeUrl)
                            withLink(
                                LinkAnnotation.Url(
                                    "https://sponsor.ajay.app/",
                                    TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary)),
                                ),
                            ) {
                                append("https://sponsor.ajay.app/")
                            }
                            append(afterUrl)
                        },
                        style = typo().bodySmall,
                    )
                },
            ) {
                SettingItem(
                    title = stringResource(Res.string.enable_sponsor_block),
                    subtitle = stringResource(Res.string.skip_sponsor_part_of_video),
                    switch = (enableSponsorBlock to { viewModel.setSponsorBlockEnabled(it) }),
                )
                val listName =
                    SponsorBlockType.toList().map { it.displayString() }
                SettingItem(
                    title = stringResource(Res.string.categories_sponsor_block),
                    subtitle = stringResource(Res.string.what_segments_will_be_skipped),
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.categories_sponsor_block) },
                                multipleSelect =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listName
                                                .mapIndexed { index, item ->
                                                    (
                                                        skipSegments?.contains(
                                                            SponsorBlockType.toList().getOrNull(index)?.value,
                                                        ) == true
                                                    ) to item
                                                }.also {
                                                    Logger.w("SettingScreen", "SettingAlertState: $skipSegments")
                                                    Logger.w("SettingScreen", "SettingAlertState: $it")
                                                },
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.save) } to { state ->
                                        viewModel.setSponsorBlockCategories(
                                            state.multipleSelect
                                                ?.getListSelected()
                                                ?.map { selected ->
                                                    listName.indexOf(selected)
                                                }?.mapNotNull { s ->
                                                    SponsorBlockType.toList().getOrNull(s).let {
                                                        it?.value
                                                    }
                                                }?.toCollection(ArrayList()) ?: arrayListOf(),
                                        )
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                    isEnable = enableSponsorBlock,
                )
            }
        }
        if (getPlatform() == Platform.Android) {
            item(key = "storage") {
                SettingGroup(title = stringResource(Res.string.storage)) {
                    SettingItem(
                        title = stringResource(Res.string.player_cache),
                        subtitle = "${playerCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(Res.string.clear_player_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(Res.string.clear) } to {
                                            viewModel.clearPlayerCache()
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.downloaded_cache),
                        subtitle = "${downloadedCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(Res.string.clear_downloaded_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(Res.string.clear) } to {
                                            viewModel.clearDownloadedCache()
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.thumbnail_cache),
                        subtitle = "${thumbnailCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(Res.string.clear_thumbnail_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(Res.string.clear) } to {
                                            viewModel.clearThumbnailCache(platformContext)
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.spotify_canvas_cache),
                        subtitle = "${canvasCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(Res.string.clear_canvas_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(Res.string.clear) } to {
                                            viewModel.clearCanvasCache()
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.limit_player_cache),
                        subtitle = LIMIT_CACHE_SIZE.getItemFromData(limitPlayerCache).toString(),
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.limit_player_cache) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                LIMIT_CACHE_SIZE.items.map { item ->
                                                    (item == LIMIT_CACHE_SIZE.getItemFromData(limitPlayerCache)) to item.toString()
                                                },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            viewModel.setPlayerCacheLimit(
                                                LIMIT_CACHE_SIZE.getDataFromItem(state.selectOne?.getSelected()),
                                            )
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    Box(
                        Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 16.dp,
                        ),
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .onGloballyPositioned { layoutCoordinates ->
                                        with(localDensity) {
                                            width =
                                                layoutCoordinates.size.width
                                                    .toDp()
                                                    .value
                                                    .toInt()
                                        }
                                    },
                        ) {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.otherApp * width).dp,
                                            ).background(
                                                md_theme_dark_primary,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.downloadCache * width).dp,
                                            ).background(
                                                Color(0xD540FF17),
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.playerCache * width).dp,
                                            ).background(
                                                Color(0xD5FFFF00),
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.canvasCache * width).dp,
                                            ).background(
                                                Color.Cyan,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.thumbCache * width).dp,
                                            ).background(
                                                Color.Magenta,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.appDatabase * width).dp,
                                            ).background(
                                                Color.White,
                                            ),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.freeSpace * width).dp,
                                            ).background(
                                                Color.DarkGray,
                                            ).fillMaxHeight(),
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    md_theme_dark_primary,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.other_app), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Green,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.downloaded_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Yellow,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.player_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Cyan,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.spotify_canvas_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Magenta,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.thumbnail_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.database), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.LightGray,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.free_space), style = typo().bodySmall)
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
        item(key = "about_us") {
            SettingGroup(title = stringResource(Res.string.about_us)) {
                SettingItem(
                    title = stringResource(Res.string.version),
                    subtitle = stringResource(Res.string.version_format, VersionManager.getVersionName()),
                )
            }
        }
        item(key = "developer_option") {
            SettingGroup(title = stringResource(Res.string.developer_option)) {
                SettingItem(
                    title = stringResource(Res.string.app_log),
                    subtitle = stringResource(Res.string.app_log_description),
                    onClick = {
                        navController.navigate(AppLogDestination)
                    },
                )
            }
        }
        item(key = "end") {
            EndOfPage()
        }
    }
    importState?.let { progress ->
        ImportProgressDialog(
            progress = progress,
            onDismiss = importViewModel::dismiss,
        )
    }
    val showLoadingDialog by viewModel.showLoadingDialog.collectAsStateWithLifecycle()
    if (showLoadingDialog.first) {
        LoadingDialog(
            true,
            showLoadingDialog.second,
        )
    }
    val basisAlertData by viewModel.basicAlertData.collectAsStateWithLifecycle()
    if (basisAlertData != null) {
        val alertBasicState = basisAlertData ?: return
        AlertDialog(
            onDismissRequest = { viewModel.setBasicAlertData(null) },
            title = {
                Text(
                    text = alertBasicState.title,
                    style = typo().titleSmall,
                )
            },
            text = {
                if (alertBasicState.message != null) {
                    Text(text = alertBasicState.message)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertBasicState.confirm.second.invoke()
                        viewModel.setBasicAlertData(null)
                    },
                ) {
                    Text(text = alertBasicState.confirm.first)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setBasicAlertData(null)
                    },
                ) {
                    Text(text = alertBasicState.dismiss)
                }
            },
        )
    }
    if (showYouTubeAccountDialog) {
        BasicAlertDialog(
            onDismissRequest = { },
            modifier = Modifier.wrapContentSize(),
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = AlertDialogDefaults.TonalElevation,
                shadowElevation = 1.dp,
            ) {
                val googleAccounts by viewModel.googleAccounts.collectAsStateWithLifecycle(
                    minActiveState = Lifecycle.State.RESUMED,
                )
                LaunchedEffect(googleAccounts) {
                    Logger.w(
                        "SettingScreen",
                        "LaunchedEffect: ${
                            googleAccounts.data?.map {
                                it.name to it.isUsed
                            }
                        }",
                    )
                }
                LazyColumn(modifier = Modifier.padding(8.dp)) {
                    item {
                        Box(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                        ) {
                            IconButton(
                                onClick = { showYouTubeAccountDialog = false },
                                colors =
                                    IconButtonDefaults.iconButtonColors().copy(
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                    ),
                                modifier =
                                    Modifier
                                        .align(Alignment.CenterStart)
                                        .fillMaxHeight(),
                            ) {
                                Icon(SimpIcons.Close, null, tint = MaterialTheme.colorScheme.onSurface)
                            }
                            Text(
                                stringResource(Res.string.youtube_account),
                                style = typo().titleMedium,
                                modifier =
                                    Modifier
                                        .align(Alignment.Center)
                                        .wrapContentHeight(align = Alignment.CenterVertically)
                                        .wrapContentWidth(),
                            )
                        }
                    }
                    if (googleAccounts is LocalResource.Success) {
                        val data = googleAccounts.data
                        if (data.isNullOrEmpty()) {
                            item {
                                Text(
                                    stringResource(Res.string.no_account),
                                    style = typo().bodyMedium,
                                    textAlign = TextAlign.Center,
                                    modifier =
                                        Modifier
                                            .padding(12.dp)
                                            .fillMaxWidth(),
                                )
                            }
                        } else {
                            items(data) {
                                Row(
                                    modifier =
                                        Modifier
                                            .padding(vertical = 8.dp)
                                            .clickable {
                                                viewModel.setUsedAccount(it)
                                            },
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Spacer(Modifier.width(24.dp))
                                    AsyncImage(
                                        model =
                                            ImageRequest
                                                .Builder(LocalPlatformContext.current)
                                                .data(it.thumbnailUrl)
                                                .crossfade(550)
                                                .build(),
                                        placeholder = rememberVectorPainter(SimpIcons.PeopleAlt),
                                        error = rememberVectorPainter(SimpIcons.PeopleAlt),
                                        contentDescription = it.name,
                                        modifier =
                                            Modifier
                                                .size(48.dp)
                                                .clip(CircleShape),
                                    )
                                    Spacer(Modifier.width(12.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(it.name, style = typo().labelMedium, color = MaterialTheme.colorScheme.onBackground)
                                        Text(it.email, style = typo().bodySmall)
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    AnimatedVisibility(it.isUsed) {
                                        Text(
                                            stringResource(Res.string.signed_in),
                                            style = typo().bodySmall,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            modifier = Modifier.widthIn(0.dp, 64.dp),
                                        )
                                    }
                                    Spacer(Modifier.width(24.dp))
                                }
                            }
                        }
                    } else {
                        item {
                            CenterLoadingBox(
                                Modifier
                                    .fillMaxWidth()
                                    .height(80.dp),
                            )
                        }
                    }
                    item {
                        Column {
                            ActionButton(
                                icon = SimpIcons.PeopleAlt,
                                text = Res.string.guest,
                            ) {
                                viewModel.setUsedAccount(null)
                                showYouTubeAccountDialog = false
                            }
                            ActionButton(
                                icon = SimpIcons.Close,
                                text = Res.string.log_out,
                            ) {
                                viewModel.setBasicAlertData(
                                    SettingBasicAlertState(
                                        title = runBlocking { getString(Res.string.warning) },
                                        message = runBlocking { getString(Res.string.log_out_warning) },
                                        confirm =
                                            runBlocking { getString(Res.string.log_out) } to {
                                                viewModel.logOutAllYouTube()
                                                showYouTubeAccountDialog = false
                                            },
                                        dismiss = runBlocking { getString(Res.string.cancel) },
                                    ),
                                )
                            }
                            ActionButton(
                                icon = SimpIcons.PlaylistAdd,
                                text = Res.string.add_an_account,
                            ) {
                                showYouTubeAccountDialog = false
                                navController.navigate(LoginDestination)
                            }
                        }
                    }
                }
            }
        }
    }
    val alertData by viewModel.alertData.collectAsStateWithLifecycle()
    if (alertData != null) {
        val alertState = alertData ?: return
        // AlertDialog
        AlertDialog(
            onDismissRequest = { viewModel.setAlertData(null) },
            title = {
                Text(
                    text = alertState.title,
                    style = typo().titleSmall,
                )
            },
            text = {
                if (alertState.message != null) {
                    Column {
                        Text(text = alertState.message)
                        if (alertState.textField != null) {
                            val verify =
                                alertState.textField.verifyCodeBlock?.invoke(
                                    alertState.textField.value,
                                ) ?: (true to null)
                            TextField(
                                value = alertState.textField.value,
                                onValueChange = {
                                    viewModel.setAlertData(
                                        alertState.copy(
                                            textField =
                                                alertState.textField.copy(
                                                    value = it,
                                                ),
                                        ),
                                    )
                                },
                                isError = !verify.first,
                                label = { Text(text = alertState.textField.label) },
                                supportingText = {
                                    if (!verify.first) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = verify.second ?: "",
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (!verify.first) {
                                        SimpIcons.Error
                                    }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 6.dp,
                                        ),
                            )
                        }
                    }
                } else if (alertState.selectOne != null) {
                    LazyColumn(
                        Modifier
                            .padding(vertical = 6.dp)
                            .heightIn(0.dp, 500.dp),
                    ) {
                        items(alertState.selectOne.listSelect) { item ->
                            val onSelect = {
                                viewModel.setAlertData(
                                    alertState.copy(
                                        selectOne =
                                            alertState.selectOne.copy(
                                                listSelect =
                                                    alertState.selectOne.listSelect.toMutableList().map {
                                                        if (it == item) {
                                                            true to it.second
                                                        } else {
                                                            false to it.second
                                                        }
                                                    },
                                            ),
                                    ),
                                )
                            }
                            Row(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSelect.invoke()
                                    }.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = item.first,
                                    onClick = {
                                        onSelect.invoke()
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.second,
                                    style = typo().bodyMedium,
                                    maxLines = 1,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(align = Alignment.CenterVertically)
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately,
                                            ).focusable(),
                                )
                            }
                        }
                    }
                } else if (alertState.multipleSelect != null) {
                    LazyColumn(
                        Modifier.padding(vertical = 6.dp),
                    ) {
                        items(alertState.multipleSelect.listSelect) { item ->
                            val onCheck = {
                                viewModel.setAlertData(
                                    alertState.copy(
                                        multipleSelect =
                                            alertState.multipleSelect.copy(
                                                listSelect =
                                                    alertState.multipleSelect.listSelect.toMutableList().map {
                                                        if (it == item) {
                                                            !it.first to it.second
                                                        } else {
                                                            it
                                                        }
                                                    },
                                            ),
                                    ),
                                )
                            }
                            Row(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onCheck.invoke()
                                    }.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = item.first,
                                    onCheckedChange = {
                                        onCheck.invoke()
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text = item.second, style = typo().bodyMedium, maxLines = 1)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertState.confirm.second.invoke(alertState)
                        viewModel.setAlertData(null)
                    },
                    enabled =
                        if (alertState.textField?.verifyCodeBlock != null) {
                            alertState.textField.verifyCodeBlock
                                .invoke(
                                    alertState.textField.value,
                                ).first
                        } else {
                            true
                        },
                ) {
                    Text(text = alertState.confirm.first)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setAlertData(null)
                    },
                ) {
                    Text(text = alertState.dismiss)
                }
            },
        )
    }


    // The pinned copy of the toolbar, frosted: the frost waits for the Devices group to go under it,
    // because an always-on frost dimmed the glow behind the bar into a black band, which is exactly
    // where the glow carries its colour.
    // Captured outside the haze scope — HazeEffectScope is not composable (same move as
    // AlbumScreen's mutedPaletteBg capture).
    val settingBarTint = MaterialTheme.colorScheme.background
    AnimatedVisibility(
        visible = showPinnedBar,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically(),
    ) {
        SettingTopBar(
            onBack = { navController.navigateUp() },
            // The house recipe from AlbumScreen's bars, thinned: ultraThin's built-in tint stacked
            // on this page's dark ground read as a solid lid. 0.3 keeps the blur doing the work and
            // the tint only settling legibility.
            modifier = Modifier.hazeBlur(HazeInput.Sources(hazeState), barBlurStyle(settingBarTint, 0.3f)),
        )
    }
}

/**
 * The page's toolbar. Drawn twice, the way Playlist and Artist draw theirs: once inside the list,
 * where it scrolls away with the content, and once pinned above it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = TopAppBarDefaults.windowInsets,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.settings),
                style = typo().titleMedium,
            )
        },
        navigationIcon = {
            Box(Modifier.padding(horizontal = 5.dp)) {
                RippleIconButton(
                    SimpIcons.ArrowBackIosNew,
                    Modifier.size(32.dp),
                    true,
                    tint = MaterialTheme.colorScheme.onSurface,
                ) {
                    onBack()
                }
            }
        },
        windowInsets = windowInsets,
        modifier = modifier,
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
            ),
    )
}

/**
 * Progress and outcome of a playlist import.
 *
 * Only dismissible once the import has finished — cancelling mid-write would leave the database
 * half-populated with no way to tell the user which half.
 */
@Composable
private fun ImportProgressDialog(
    progress: ImportProgress,
    onDismiss: () -> Unit,
) {
    val finished = progress is ImportProgress.Success || progress is ImportProgress.Error
    AlertDialog(
        onDismissRequest = { if (finished) onDismiss() },
        properties =
            DialogProperties(
                dismissOnBackPress = finished,
                dismissOnClickOutside = finished,
            ),
        title = {
            Text(
                text =
                    stringResource(
                        if (progress is ImportProgress.Error) Res.string.import_failed else Res.string.import_data,
                    ),
                style = typo().titleSmall,
            )
        },
        text = {
            Column {
                when (progress) {
                    is ImportProgress.Preparing -> {
                        Text(
                            text = stringResource(Res.string.import_reading_file),
                            style = typo().bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    is ImportProgress.Importing -> {
                        Text(
                            text = stringResource(Res.string.import_progress_songs, progress.processed, progress.total),
                            style = typo().bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (progress.total > 0) progress.processed.toFloat() / progress.total else 0f
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    is ImportProgress.Success -> {
                        Text(
                            text =
                                stringResource(
                                    Res.string.import_result,
                                    progress.result.playlistsCreated,
                                    progress.result.songsImported,
                                ),
                            style = typo().bodyMedium,
                        )
                        if (progress.result.skippedEntries > 0) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.import_result_skipped, progress.result.skippedEntries),
                                style = typo().bodySmall,
                            )
                        }
                    }

                    is ImportProgress.Error -> {
                        Text(
                            text = progress.message,
                            style = typo().bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (finished) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(Res.string.ok))
                }
            }
        },
    )
}