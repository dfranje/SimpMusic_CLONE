package com.maxrave.simpmusic.ui.screen.xyz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.maxrave.simpmusic.expect.ui.rememberLeaveApp
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.Artist
import com.maxrave.simpmusic.ui.icon.Favorite
import com.maxrave.simpmusic.ui.icon.Home
import com.maxrave.simpmusic.ui.icon.MusicNote
import com.maxrave.simpmusic.ui.icon.Settings
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.home.SettingsDestination
import com.maxrave.simpmusic.ui.navigation.destination.xyz.XyzArtistsDestination
import com.maxrave.simpmusic.ui.navigation.destination.xyz.XyzFavoritesDestination
import com.maxrave.simpmusic.ui.navigation.destination.xyz.XyzSongsDestination
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.xyzRim
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.artists
import simpmusic.composeapp.generated.resources.home
import simpmusic.composeapp.generated.resources.settings
import simpmusic.composeapp.generated.resources.songs
import simpmusic.composeapp.generated.resources.xyz_back
import simpmusic.composeapp.generated.resources.xyz_favorites
import kotlin.reflect.KClass

/** One sidebar page of the XYZ shell. */
private class XyzNavItem(
    val destination: Any,
    val route: KClass<*>,
    val title: StringResource,
    val icon: ImageVector,
)

private val XyzNavItems =
    listOf(
        XyzNavItem(HomeDestination, HomeDestination::class, Res.string.home, SimpIcons.Home),
        XyzNavItem(XyzArtistsDestination, XyzArtistsDestination::class, Res.string.artists, SimpIcons.Artist),
        XyzNavItem(XyzSongsDestination, XyzSongsDestination::class, Res.string.songs, SimpIcons.MusicNote),
        XyzNavItem(XyzFavoritesDestination, XyzFavoritesDestination::class, Res.string.xyz_favorites, SimpIcons.Favorite),
        XyzNavItem(SettingsDestination, SettingsDestination::class, Res.string.settings, SimpIcons.Settings),
    )

private fun NavDestination?.isOn(item: XyzNavItem): Boolean = this?.hierarchy?.any { it.hasRoute(item.route) } == true

/** Opens a sidebar page the way the stock tabs do: one copy of each, its scroll and state kept. */
fun NavController.navigateXyzPage(destination: Any) {
    navigate(destination) {
        popUpTo(graph.startDestinationId) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

/**
 * The XYZ shell's sidebar, drawn like XYZ Glazba's: one rounded card with the accent rim, the pages at
 * the top with the open one filled with the accent, and Back at the bottom. Back steps back through
 * the app and, from Home, returns to the launcher with the music still playing.
 */
@Composable
fun XyzSideBar(navController: NavController) {
    val p = xyzPalette()
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination
    val leaveApp = rememberLeaveApp()
    val corner = p.corner(28.dp)
    val shape = RoundedCornerShape(corner)
    Column(
        modifier =
            Modifier
                .fillMaxHeight()
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical + WindowInsetsSides.Start))
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp)
                .width(SideBarWidth)
                .xyzRim(corner, shape)
                .clip(shape)
                .background(p.cardBrush, shape)
                .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Scrolls only where the height runs out (a phone on its side); a head unit fits every page.
        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            XyzNavItems.forEach { item ->
                SideBarItem(
                    icon = item.icon,
                    label = stringResource(item.title),
                    selected = destination.isOn(item),
                ) { navController.navigateXyzPage(item.destination) }
            }
        }
        SideBarItem(
            icon = SimpIcons.ArrowBackIosNew,
            label = stringResource(Res.string.xyz_back),
            selected = false,
        ) {
            if (navController.previousBackStackEntry != null) navController.popBackStack() else leaveApp()
        }
    }
}

private val SideBarWidth = 92.dp

@Composable
private fun SideBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val p = xyzPalette()
    val shape = RoundedCornerShape(p.corner(18.dp))
    val content = if (selected) MaterialTheme.colorScheme.onPrimary else p.text
    Column(
        Modifier
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (selected) Brush.verticalGradient(listOf(p.accent, p.accentDeep)) else Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
                shape,
            ).selectable(selected = selected, role = Role.Tab, onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(26.dp))
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            style = typo().bodySmall,
            color = if (selected) content else p.textDim,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** On a phone held upright: the same five pages along the bottom, in one of the launcher's cards. */
@Composable
fun XyzBottomBar(navController: NavController) {
    val p = xyzPalette()
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination
    XyzCard(
        modifier =
            Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 12.dp, vertical = 8.dp),
        corner = 24.dp,
    ) {
        Row(Modifier.fillMaxWidth().padding(6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            XyzNavItems.forEach { item ->
                val selected = destination.isOn(item)
                val shape = RoundedCornerShape(p.corner(16.dp))
                val content = if (selected) MaterialTheme.colorScheme.onPrimary else p.text
                Column(
                    Modifier
                        .weight(1f)
                        .clip(shape)
                        .background(if (selected) p.accent else Color.Transparent, shape)
                        .selectable(selected = selected, role = Role.Tab) { navController.navigateXyzPage(item.destination) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(item.icon, contentDescription = null, tint = content, modifier = Modifier.size(24.dp))
                    Text(
                        stringResource(item.title),
                        style = typo().bodySmall,
                        color = if (selected) content else p.textDim,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
