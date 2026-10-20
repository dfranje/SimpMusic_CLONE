package com.maxrave.simpmusic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.expect.ui.canFollowXyzLauncher
import com.maxrave.simpmusic.expect.ui.rememberXyzLauncherPalette
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.library.LibraryDynamicPlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.AlbumDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.ArtistDestination
import com.maxrave.simpmusic.ui.navigation.destination.list.PlaylistDestination
import com.maxrave.simpmusic.ui.navigation.destination.xyz.XyzFavoritesDestination
import com.maxrave.simpmusic.ui.navigation.graph.AppNavigationGraph
import com.maxrave.simpmusic.ui.screen.xyz.XyzBottomBar
import com.maxrave.simpmusic.ui.screen.xyz.XyzSideBar
import com.maxrave.simpmusic.ui.screen.xyz.navigateXyzPage
import com.maxrave.simpmusic.ui.theme.AppTheme
import com.maxrave.simpmusic.ui.theme.XYZ_ACCENT_FOLLOW
import com.maxrave.simpmusic.ui.theme.buildXyzPalette
import com.maxrave.simpmusic.ui.theme.isDarkTheme
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.xyzAccentIndex
import com.maxrave.simpmusic.ui.theme.xyzAccentSettingOrDefault
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.maxrave.simpmusic.viewModel.base.UserPlaybackStarts
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.do_not_show_again
import simpmusic.composeapp.generated.resources.good_night
import simpmusic.composeapp.generated.resources.notification
import simpmusic.composeapp.generated.resources.settings
import simpmusic.composeapp.generated.resources.sleep_timer_off
import simpmusic.composeapp.generated.resources.this_app_needs_to_access_your_notification
import simpmusic.composeapp.generated.resources.this_link_is_not_supported
import simpmusic.composeapp.generated.resources.yes
import kotlin.time.ExperimentalTime

/**
 * The app is the XYZ Launcher shell and nothing else (XyzShell.kt): Home is the player, the sidebar
 * (or, upright on a phone, the bottom bar) holds Home, Artists, Songs, Favorites and Settings.
 *
 * The look does not wait for a stored setting: it is drawn from the very first frame, in the
 * default accent until the stored one (or the launcher's palette) arrives a moment later.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class, ExperimentalFoundationApi::class)
@Composable
fun App(
    viewModel: SharedViewModel = koinInject(),
    showDesktopNotificationPermissionDialog: Boolean = false,
    onDismissDesktopNotificationPermissionDialog: (doNotShowAgain: Boolean) -> Unit = {},
    onOpenDesktopNotificationSettings: (doNotShowAgain: Boolean) -> Unit = {},
) {
    val windowSize = currentWindowAdaptiveInfo().windowSizeClass
    val navController = rememberNavController()
    val isDesktopShell = getPlatform() == Platform.Desktop

    val sleepTimerState by viewModel.sleepTimerState.collectAsStateWithLifecycle()
    val intent by viewModel.intent.collectAsStateWithLifecycle()

    val themeMode by viewModel.getThemeMode().collectAsStateWithLifecycle(DataStoreManager.THEME_MODE_DARK)
    // The launcher's own palette and day/night while following it (Android, with the launcher
    // installed), otherwise the launcher's formulas on the picked accent and this app's mode.
    val storedXyzAccent by viewModel.getXyzAccent().collectAsStateWithLifecycle(null)
    val xyzAccent = xyzAccentSettingOrDefault(storedXyzAccent, canFollowXyzLauncher())
    val launcherPalette = rememberXyzLauncherPalette(enabled = xyzAccent == XYZ_ACCENT_FOLLOW)
    val xyzDarkFromMode = isDarkTheme(themeMode)
    val xyzPalette =
        remember(launcherPalette, xyzAccent, xyzDarkFromMode) {
            launcherPalette ?: buildXyzPalette(xyzAccentIndex(xyzAccent), xyzDarkFromMode)
        }

    var isNavBarVisible by rememberSaveable {
        mutableStateOf(true)
    }

    LaunchedEffect(intent) {
        val intent = intent ?: return@LaunchedEffect
        val data = intent.data
        Logger.d("MainActivity", "onCreate: $data")
        if (data != null) {
            if (data.scheme == "wordbyword" && data.host == "lastfm-auth") {
                // Last.fm sends the user back here after they approve access, carrying the request
                // token: wordbyword://lastfm-auth?token=xxx. The callback is fixed on the API
                // account, which is why the scheme is not "simpmusic".
                val token = data.getQueryParameter("token")
                Logger.d("MainActivity", "Last.fm callback, token present: ${!token.isNullOrEmpty()}")
                viewModel.setIntent(null)
                // Deliberately no navigation: the login screen is almost certainly already open —
                // the browser was opened from it — and navigating would stack a second copy on top
                // of it. The token is handed straight to the shared view model, and the screen
                // closes itself when it sees a session key appear.
                token?.let { viewModel.completeLastfmLogin(it) }
            } else if (data.host == "simpmusic.org" || data.scheme == "simpmusic") {
                // https://simpmusic.org/app/watch?v=VIDEO_ID
                // https://simpmusic.org/app/playlist?list=PLAYLIST_ID
                // https://simpmusic.org/app/channel/CHANNEL_ID
                // simpmusic://watch?v=VIDEO_ID  (host="watch", no path)
                // simpmusic://playlist?list=PLAYLIST_ID
                // simpmusic://channel/CHANNEL_ID
                val segments = data.pathSegments
                // For simpmusic.org: segments = ["app", "watch"] → appPath = segments[1]
                // For simpmusic://: host IS the appPath (e.g. host="watch"), segments = []
                val appPath =
                    if (data.scheme == "simpmusic") {
                        data.host
                    } else {
                        segments.getOrNull(1)
                    }
                Logger.d("MainActivity", "simpmusic.org deep link, appPath: $appPath")
                viewModel.setIntent(null)
                when (appPath) {
                    "watch" -> {
                        data.getQueryParameter("v")?.let { videoId ->
                            viewModel.loadSharedMediaItem(videoId)
                        }
                    }

                    "playlist" -> {
                        data.getQueryParameter("list")?.let { playlistId ->
                            if (playlistId.startsWith("OLAK5uy_")) {
                                navController.navigate(AlbumDestination(browseId = playlistId))
                            } else if (playlistId.startsWith("VL")) {
                                navController.navigate(PlaylistDestination(playlistId = playlistId))
                            } else {
                                navController.navigate(PlaylistDestination(playlistId = "VL$playlistId"))
                            }
                        }
                    }

                    "channel", "c" -> {
                        // simpmusic://channel/UCxxx → segments = ["UCxxx"]
                        // simpmusic.org/app/channel/UCxxx → segments = ["app", "channel", "UCxxx"]
                        val artistId =
                            if (data.scheme == "simpmusic") {
                                segments.firstOrNull()
                            } else {
                                segments.getOrNull(2)
                            }
                        artistId?.let {
                            if (it.startsWith("UC")) {
                                navController.navigate(ArtistDestination(channelId = it))
                            } else {
                                viewModel.makeToast(getString(Res.string.this_link_is_not_supported))
                            }
                        }
                    }

                    "album" -> {
                        data.getQueryParameter("id")?.let { albumId ->
                            navController.navigate(AlbumDestination(browseId = albumId))
                        }
                    }

                    // simpmusic://library?type=favorite → one of the library collections. A bare
                    // simpmusic://library opens Favorites, the XYZ shell's own library page.
                    "library" -> {
                        val type = data.getQueryParameter("type")
                        if (type.isNullOrBlank()) {
                            navController.navigateXyzPage(XyzFavoritesDestination)
                        } else {
                            navController.navigate(LibraryDynamicPlaylistDestination(type = type))
                        }
                    }

                    else -> {
                        viewModel.makeToast(getString(Res.string.this_link_is_not_supported))
                    }
                }
            } else {
                Logger.d("MainActivity", "onCreate: $data")
                when (val path = data.pathSegments.firstOrNull()) {
                    "playlist" -> {
                        data
                            .getQueryParameter("list")
                            ?.let { playlistId ->
                                viewModel.setIntent(null)
                                if (playlistId.startsWith("OLAK5uy_")) {
                                    navController.navigate(
                                        AlbumDestination(
                                            browseId = playlistId,
                                        ),
                                    )
                                } else if (playlistId.startsWith("VL")) {
                                    navController.navigate(
                                        PlaylistDestination(
                                            playlistId = playlistId,
                                        ),
                                    )
                                } else {
                                    navController.navigate(
                                        PlaylistDestination(
                                            playlistId = "VL$playlistId",
                                        ),
                                    )
                                }
                            }
                    }

                    "channel", "c" -> {
                        data.lastPathSegment?.let { artistId ->
                            if (artistId.startsWith("UC")) {
                                viewModel.setIntent(null)
                                navController.navigate(
                                    ArtistDestination(
                                        channelId = artistId,
                                    ),
                                )
                            } else {
                                viewModel.makeToast(
                                    getString(
                                        Res.string.this_link_is_not_supported,
                                    ),
                                )
                            }
                        }
                    }

                    else -> {
                        when {
                            path == "watch" -> data.getQueryParameter("v")
                            data.host == "youtu.be" -> path
                            else -> null
                        }?.let { videoId ->
                            viewModel.loadSharedMediaItem(videoId)
                        }
                    }
                }
            }
        }
    }

    // Picking a song anywhere lands on the player, which in this shell is Home.
    LaunchedEffect(Unit) {
        UserPlaybackStarts.events.collect { navController.navigateXyzPage(HomeDestination) }
    }
    val isTablet = windowSize.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)

    AppTheme(xyzPalette = xyzPalette) {
        Scaffold(
            containerColor = if (isDesktopShell) xyzPalette.bgBottom else MaterialTheme.colorScheme.background,
            bottomBar = {
                if (!isTablet) {
                    AnimatedVisibility(
                        isNavBarVisible,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        XyzBottomBar(navController)
                    }
                }
            },
            content = { innerPadding ->
                Row(Modifier.fillMaxSize()) {
                    if (isTablet) {
                        XyzSideBar(navController)
                    }
                    // Desktop only: the content sits in its own rounded panel on the window's
                    // darkest tone. Phones and head units keep one continuous surface.
                    Box(
                        Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .then(
                                if (isDesktopShell) {
                                    Modifier
                                        .padding(8.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.background)
                                } else {
                                    Modifier
                                },
                            ),
                    ) {
                        AppNavigationGraph(
                            innerPadding = innerPadding,
                            navController = navController,
                            hideNavBar = {
                                isNavBarVisible = false
                            },
                            showNavBar = {
                                isNavBarVisible = true
                            },
                        )
                    }
                }

                if (sleepTimerState.isDone) {
                    Logger.w("MainActivity", "Sleep Timer Done: $sleepTimerState")
                    AlertDialog(
                        properties =
                            DialogProperties(
                                dismissOnBackPress = false,
                                dismissOnClickOutside = false,
                            ),
                        onDismissRequest = {
                            viewModel.stopSleepTimer()
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                viewModel.stopSleepTimer()
                            }) {
                                Text(
                                    stringResource(Res.string.yes),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        text = {
                            Text(
                                stringResource(Res.string.sleep_timer_off),
                                style = typo().labelSmall,
                            )
                        },
                        title = {
                            Text(
                                stringResource(Res.string.good_night),
                                style = typo().bodySmall,
                            )
                        },
                    )
                }

                // Desktop only: Android no longer asks for the notification permission.
                if (showDesktopNotificationPermissionDialog) {
                    var doNotShowAgain by remember { mutableStateOf(false) }
                    AlertDialog(
                        onDismissRequest = {
                            onDismissDesktopNotificationPermissionDialog(doNotShowAgain)
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    onOpenDesktopNotificationSettings(doNotShowAgain)
                                },
                            ) {
                                Text(
                                    stringResource(Res.string.settings),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { onDismissDesktopNotificationPermissionDialog(doNotShowAgain) }) {
                                Text(
                                    stringResource(Res.string.cancel),
                                    style = typo().bodySmall,
                                )
                            }
                        },
                        title = {
                            Text(
                                stringResource(Res.string.notification),
                                style = typo().labelSmall,
                            )
                        },
                        text = {
                            Column {
                                Text(
                                    stringResource(Res.string.this_app_needs_to_access_your_notification),
                                    style = typo().bodySmall,
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier =
                                        Modifier
                                            .clickable { doNotShowAgain = !doNotShowAgain }
                                            .fillMaxWidth(),
                                ) {
                                    Checkbox(
                                        checked = doNotShowAgain,
                                        onCheckedChange = { doNotShowAgain = it },
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        stringResource(Res.string.do_not_show_again),
                                        style = typo().bodySmall,
                                    )
                                }
                            }
                        },
                    )
                }
            },
        )
    }
}
