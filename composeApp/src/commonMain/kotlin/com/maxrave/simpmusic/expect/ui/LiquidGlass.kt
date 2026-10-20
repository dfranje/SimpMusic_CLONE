package com.maxrave.simpmusic.expect.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * What a glass surface used to sample from. The XYZ build ships no glass (Kyant's backdrop
 * library is gone, every glass surface is drawn flat), so this is only a token the call sites
 * keep handing to [com.maxrave.simpmusic.ui.component.liquidGlass].
 */
@Immutable
class PlatformBackdrop internal constructor()

/** Marked the source layer that sibling glass surfaces refracted; draws nothing now. */
@Suppress("UNUSED_PARAMETER")
fun Modifier.layerBackdrop(backdrop: PlatformBackdrop): Modifier = this

@Composable
fun rememberBackdrop(
    @Suppress("UNUSED_PARAMETER") color: Color,
): PlatformBackdrop = remember { PlatformBackdrop() }
