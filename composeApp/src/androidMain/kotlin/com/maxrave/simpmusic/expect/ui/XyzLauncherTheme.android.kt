package com.maxrave.simpmusic.expect.ui

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.ui.theme.XyzAccents
import com.maxrave.simpmusic.ui.theme.XyzPalette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.withContext

// XYZ-Media-Radio: launcher/.../data/ThemeProvider.kt. Declared in androidApp's <queries>, or Android 11+
// hides the provider from this app and every read comes back null.
private val LAUNCHER_THEME_URI: Uri = Uri.parse("content://xyz.exyu.launcher.theme/palette")
private const val TAG = "XyzLauncherTheme"

actual fun canFollowXyzLauncher(): Boolean = true

@Composable
actual fun rememberXyzLauncherPalette(enabled: Boolean): XyzPalette? {
    val context = LocalContext.current.applicationContext
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val palette by produceState<XyzPalette?>(initialValue = null, context, lifecycle, enabled) {
        if (!enabled) {
            value = null
            return@produceState
        }
        val reload = Channel<Unit>(Channel.CONFLATED)
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    reload.trySend(Unit)
                }
            }
        // Coming back to the front re-reads too: the launcher can change while this app is away, and a
        // launcher installed after start only becomes observable once registering succeeds.
        val resumeObserver =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) reload.trySend(Unit)
            }
        lifecycle.addObserver(resumeObserver)
        var observing = false
        reload.trySend(Unit)
        try {
            for (ignored in reload) {
                if (!observing) {
                    // Throws SecurityException on Android 8+ while no provider answers for the authority.
                    observing =
                        runCatching {
                            context.contentResolver.registerContentObserver(LAUNCHER_THEME_URI, false, observer)
                        }.isSuccess
                }
                value = withContext(Dispatchers.IO) { readLauncherPalette(context) }
            }
        } finally {
            lifecycle.removeObserver(resumeObserver)
            if (observing) runCatching { context.contentResolver.unregisterContentObserver(observer) }
        }
    }
    return palette
}

private fun readLauncherPalette(context: Context): XyzPalette? =
    try {
        context.contentResolver.query(LAUNCHER_THEME_URI, null, null, null, null)?.use { c ->
            if (!c.moveToFirst()) return@use null
            fun int(column: String): Int = c.getInt(c.getColumnIndexOrThrow(column))

            fun color(column: String): Color = Color(int(column))

            fun float(column: String): Float = c.getFloat(c.getColumnIndexOrThrow(column))
            XyzPalette(
                dark = int("dark") == 1,
                accentIndex = int("palette").coerceIn(XyzAccents.indices),
                accent = color("accent"),
                accentDeep = color("accent_deep"),
                accent2 = color("accent2"),
                bgTop = color("bg_top"),
                bgBottom = color("bg_bottom"),
                cardTop = color("card_top"),
                cardBottom = color("card_bottom"),
                border = color("border"),
                tile = color("tile"),
                text = color("text"),
                textDim = color("text_dim"),
                borderOn = int("border_on") == 1,
                cornerMul = float("corner_mul"),
                glow = float("glow"),
                fromLauncher = true,
            )
        }
    } catch (e: Exception) {
        // Not installed (null cursor, no exception), an older launcher without a column, or the
        // provider died mid-read: the XYZ look then runs on the accent picked in Settings.
        Logger.d(TAG, "Launcher palette unavailable: ${e.message}")
        null
    }
