package com.maxrave.simpmusic.ui.navigation.destination.xyz

import kotlinx.serialization.Serializable

// The XYZ Launcher shell's own sidebar pages (XyzShell.kt). Home and Settings reuse HomeDestination
// and SettingsDestination: Home simply draws the player there while the XYZ look is on.

@Serializable
data object XyzArtistsDestination

@Serializable
data object XyzSongsDestination

@Serializable
data object XyzFavoritesDestination
