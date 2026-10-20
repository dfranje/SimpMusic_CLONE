package com.maxrave.simpmusic.ui.navigation.graph

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.maxrave.simpmusic.ui.navigation.destination.home.HomeDestination
import com.maxrave.simpmusic.ui.navigation.destination.xyz.XyzArtistsDestination
import com.maxrave.simpmusic.ui.navigation.destination.xyz.XyzFavoritesDestination
import com.maxrave.simpmusic.ui.navigation.destination.xyz.XyzSongsDestination
import com.maxrave.simpmusic.ui.screen.xyz.XyzArtistsScreen
import com.maxrave.simpmusic.ui.screen.xyz.XyzFavoritesScreen
import com.maxrave.simpmusic.ui.screen.xyz.XyzHomeScreen
import com.maxrave.simpmusic.ui.screen.xyz.XyzSongsScreen

@Composable
@ExperimentalMaterial3Api
@ExperimentalFoundationApi
fun AppNavigationGraph(
    innerPadding: PaddingValues,
    navController: NavHostController,
    startDestination: Any = HomeDestination,
    hideNavBar: () -> Unit = { },
    showNavBar: () -> Unit = { },
) {
    NavHost(
        navController,
        startDestination = startDestination,
        enterTransition = {
            fadeIn() + slideInHorizontally { -it }
        },
        exitTransition = {
            fadeOut() + slideOutHorizontally { it }
        },
        popEnterTransition = {
            fadeIn() + slideInHorizontally { -it }
        },
        popExitTransition = {
            fadeOut() + slideOutHorizontally { it }
        },
    ) {
        // The XYZ shell's own pages (XyzShell.kt): Home is the player.
        composable<HomeDestination> {
            XyzHomeScreen(
                navController = navController,
                innerPadding = innerPadding,
            )
        }
        composable<XyzArtistsDestination> {
            XyzArtistsScreen(
                navController = navController,
                innerPadding = innerPadding,
            )
        }
        composable<XyzSongsDestination> {
            XyzSongsScreen(innerPadding = innerPadding)
        }
        composable<XyzFavoritesDestination> {
            XyzFavoritesScreen(
                navController = navController,
                innerPadding = innerPadding,
            )
        }
        homeScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        libraryScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        listScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
        )
        loginScreenGraph(
            innerPadding = innerPadding,
            navController = navController,
            hideBottomBar = hideNavBar,
            showBottomBar = showNavBar,
        )
    }
}
