package com.maxrave.simpmusic.ui.theme

import androidx.compose.foundation.border
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.StringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.xyz_accent_amber
import simpmusic.composeapp.generated.resources.xyz_accent_blue
import simpmusic.composeapp.generated.resources.xyz_accent_green
import simpmusic.composeapp.generated.resources.xyz_accent_orange
import simpmusic.composeapp.generated.resources.xyz_accent_pink
import simpmusic.composeapp.generated.resources.xyz_accent_purple
import simpmusic.composeapp.generated.resources.xyz_accent_red
import simpmusic.composeapp.generated.resources.xyz_accent_teal
import kotlin.math.pow

/**
 * The XYZ Launcher look: the launcher's own palettes (`launcher/.../ui/Theme.kt` in XYZ-Media-Radio),
 * ported so SimpMusic reads as part of the same head unit — neon accent on near-black surfaces tinted
 * by that accent, cards with an accent rim and a soft glow.
 *
 * It is the app's only look. With the launcher installed, Android follows its palette and its
 * day/night live ([XyzPalette.fromLauncher]); without it, or on Desktop, the same formulas run here
 * on the accent picked in Settings.
 */

/** Generic DataStore string: [XYZ_ACCENT_FOLLOW], or an index into [XyzAccents]. */
const val XYZ_ACCENT_KEY = "xyz_accent"

/** Take accent and day/night from the XYZ Launcher itself. */
const val XYZ_ACCENT_FOLLOW = "follow"

/** Set once the light build's defaults (see SharedViewModel.applyXyzLightDefaultsOnce) were applied. */
const val XYZ_LIGHT_DEFAULTS_KEY = "xyz_light_defaults_v1"

/** One launcher palette: [accent] is the main colour, [deep] its darker end, [accent2] the second colour. */
@Immutable
class XyzAccent(
    val name: StringResource,
    val accent: Color,
    val deep: Color,
    val accent2: Color,
)

/** Same order and values as the launcher's ACCENTS: the launcher hands over an index into this list. */
val XyzAccents =
    listOf(
        XyzAccent(Res.string.xyz_accent_purple, Color(0xFFA855F7), Color(0xFF7C3AED), Color(0xFFE879F9)),
        XyzAccent(Res.string.xyz_accent_orange, Color(0xFFFF9F1C), Color(0xFFFF6A00), Color(0xFFFFD166)),
        XyzAccent(Res.string.xyz_accent_red, Color(0xFFFF2D3D), Color(0xFFC1121F), Color(0xFFFF6B4A)),
        XyzAccent(Res.string.xyz_accent_pink, Color(0xFFFF3EA5), Color(0xFFD61F84), Color(0xFFC084FC)),
        XyzAccent(Res.string.xyz_accent_blue, Color(0xFF2F8BFF), Color(0xFF1D4ED8), Color(0xFF38E1FF)),
        XyzAccent(Res.string.xyz_accent_teal, Color(0xFF14E0D0), Color(0xFF0A9F97), Color(0xFF5EF2A8)),
        XyzAccent(Res.string.xyz_accent_green, Color(0xFF7CFC32), Color(0xFF2FB82A), Color(0xFF3BC8FF)),
        XyzAccent(Res.string.xyz_accent_amber, Color(0xFFFFC21A), Color(0xFFE08A00), Color(0xFFFFE45C)),
    )

/**
 * A resolved launcher palette: what the launcher draws with right now when [fromLauncher], otherwise
 * the launcher's defaults (gradient background, rounded corners, rim and normal glow) for the accent
 * at [accentIndex].
 */
@Immutable
data class XyzPalette(
    val dark: Boolean,
    val accentIndex: Int,
    val accent: Color,
    val accentDeep: Color,
    val accent2: Color,
    val bgTop: Color,
    val bgBottom: Color,
    val cardTop: Color,
    val cardBottom: Color,
    val border: Color,
    val tile: Color,
    val text: Color,
    val textDim: Color,
    val borderOn: Boolean = true,
    val cornerMul: Float = 1f,
    val glow: Float = 1f,
    val fromLauncher: Boolean = false,
) {
    /** The launcher's card fill, top to bottom. */
    val cardBrush: Brush get() = Brush.verticalGradient(listOf(cardTop, cardBottom))

    /** A corner of [base] scaled the way the launcher's sharp / rounded / very rounded setting scales its own. */
    fun corner(base: Dp): Dp = base * cornerMul
}

/** The XYZ palette in effect, or null outside [AppTheme] (the Desktop mini-player window, previews). */
val LocalXyzPalette = staticCompositionLocalOf<XyzPalette?> { null }

/**
 * The app accent for the places that paint the brand colour directly rather than a scheme role (the
 * playing row, active shuffle/repeat, the sleep timer): the launcher's accent, or [seed] outside
 * [AppTheme].
 */
val appAccent: Color
    @Composable
    @ReadOnlyComposable
    get() = LocalXyzPalette.current?.accent ?: seed

/** The stored accent setting, or its default: follow the launcher on Android, the first palette elsewhere. */
fun xyzAccentSettingOrDefault(
    stored: String?,
    canFollowLauncher: Boolean,
): String = stored?.takeIf { it == XYZ_ACCENT_FOLLOW && canFollowLauncher || it.toIntOrNull() in XyzAccents.indices }
    ?: if (canFollowLauncher) XYZ_ACCENT_FOLLOW else "0"

/** Index into [XyzAccents] for a stored accent setting; "follow" falls back to the first palette. */
fun xyzAccentIndex(setting: String): Int = setting.toIntOrNull()?.coerceIn(XyzAccents.indices) ?: 0

/**
 * The launcher's buildPalette() for its default look (screen background "gradient", rounded corners,
 * rim on, normal glow), for [accentIndex] in [dark] or day mode.
 */
fun buildXyzPalette(
    accentIndex: Int,
    dark: Boolean,
): XyzPalette {
    val index = accentIndex.coerceIn(XyzAccents.indices)
    val a = XyzAccents[index]
    return if (dark) {
        XyzPalette(
            dark = true,
            accentIndex = index,
            accent = a.accent,
            accentDeep = a.deep,
            accent2 = a.accent2,
            bgTop = tone(a.accent, 0.55f, 0.075f),
            bgBottom = tone(a.accent, 0.40f, 0.025f),
            cardTop = tone(a.accent, 0.42f, 0.115f),
            cardBottom = tone(a.accent, 0.38f, 0.055f),
            border = a.accent.copy(alpha = 0.55f),
            tile = tone(a.accent, 0.32f, 0.16f),
            text = Color.White,
            textDim = tone(a.accent, 0.10f, 0.78f),
        )
    } else {
        val accent = forLight(a.accent)
        XyzPalette(
            dark = false,
            accentIndex = index,
            accent = accent,
            accentDeep = forLight(a.deep),
            accent2 = forLight(a.accent2),
            bgTop = lerp(Color(0xFFF5F5FA), accent, 0.08f),
            bgBottom = lerp(Color(0xFFE2E3EE), accent, 0.16f),
            cardTop = Color.White,
            cardBottom = lerp(Color(0xFFF3F4FB), accent, 0.04f),
            border = lerp(Color(0xFFC9CDE6), accent, 0.35f),
            tile = lerp(Color(0xFFE9EAF4), accent, 0.16f),
            text = Color(0xFF14162C),
            textDim = Color(0xFF5B6088),
        )
    }
}

/**
 * [base] (generated from the accent, which supplies the container roles) with the launcher's own
 * colours on every role the screens actually paint with: the accent itself as primary rather than
 * Material's pastel tone of it, and the launcher's tinted surfaces, text and rim.
 */
fun ColorScheme.withXyzPalette(p: XyzPalette): ColorScheme {
    val page = p.bgTop
    val card = p.cardTop
    return copy(
        primary = p.accent,
        onPrimary = contentOn(p.accent),
        secondary = p.accent2,
        onSecondary = contentOn(p.accent2),
        inversePrimary = p.accentDeep,
        surfaceTint = p.accent,
        background = page,
        onBackground = p.text,
        surface = page,
        onSurface = p.text,
        surfaceVariant = p.tile,
        onSurfaceVariant = p.textDim,
        surfaceDim = p.bgBottom,
        surfaceBright = lerp(p.tile, p.text, 0.08f),
        surfaceContainerLowest = if (p.dark) p.bgBottom else Color.White,
        surfaceContainerLow = lerp(page, card, 0.5f),
        surfaceContainer = if (p.dark) card else p.cardBottom,
        surfaceContainerHigh = if (p.dark) lerp(card, p.tile, 0.5f) else lerp(p.cardBottom, p.tile, 0.5f),
        surfaceContainerHighest = p.tile,
        outline = p.border.compositeOver(card),
        outlineVariant = lerp(card, p.accent, if (p.dark) 0.22f else 0.18f),
        inverseSurface = p.text,
        inverseOnSurface = p.bgBottom,
    )
}

/**
 * The launcher's card rim and neon glow (its GlassCard + neonGlow): strokes of the accent, wider and
 * fainter outwards, drawn around [shape]'s edge before the rim itself. Put it BEFORE any clip in the
 * chain, or the glow is cut off with the content. A no-op outside the XYZ look; the glow only shows
 * at night with the rim on, like in the launcher.
 */
@Composable
fun Modifier.xyzRim(
    corner: Dp,
    shape: Shape,
    glow: Boolean = true,
): Modifier {
    val p = LocalXyzPalette.current ?: return this
    if (!p.borderOn) return this
    var m = this
    if (glow && p.dark && p.glow > 0f) {
        val strength = p.glow
        val color = p.accent
        m =
            m.drawBehind {
                val r = corner.toPx()
                val u = 1.dp.toPx()
                // Centred on the edge: the card covers the inner half, the outer half is what shows.
                GlowStrokes.forEach { (width, alpha) ->
                    val sw = width * u
                    drawRoundRect(
                        color = color.copy(alpha = (alpha * strength).coerceAtMost(0.5f)),
                        cornerRadius = CornerRadius(r + sw / 4f),
                        style = Stroke(width = sw),
                    )
                }
            }
    }
    return m.border(1.2.dp, p.border, shape)
}

// The launcher's glow: stroke width (design units) to alpha.
private val GlowStrokes = listOf(5f to 0.20f, 10f to 0.10f, 17f to 0.055f, 26f to 0.028f)

/** Icons and text on an accent fill: white like the launcher, dark only on accents too bright for it. */
private fun contentOn(c: Color): Color = if (c.luminance() > 0.5f) Color(0xFF14162C) else Color.White

/** Colour in [accent]'s hue at the given saturation and value (HSV): the launcher's tinted dark surfaces. */
private fun tone(
    accent: Color,
    saturation: Float,
    value: Float,
): Color = Color.hsv(hueOf(accent), saturation.coerceIn(0f, 1f), value.coerceIn(0f, 1f))

private fun hueOf(c: Color): Float {
    val max = maxOf(c.red, c.green, c.blue)
    val min = minOf(c.red, c.green, c.blue)
    val d = max - min
    if (d == 0f) return 0f
    val sector =
        when (max) {
            c.red -> ((c.green - c.blue) / d) % 6f
            c.green -> (c.blue - c.red) / d + 2f
            else -> (c.red - c.green) / d + 4f
        }
    val h = sector * 60f
    return (if (h < 0f) h + 360f else h).coerceIn(0f, 360f)
}

/**
 * The launcher's day accent: bright accents (lime, amber, teal) wash out on white, so they are darkened
 * until their relative luminance is at most 0.28. Purple, red and blue stay as they are.
 */
private fun forLight(c: Color): Color {
    val l = c.luminance()
    if (l <= 0.28f) return c
    val k = (0.28f / l).pow(1f / 2.2f)
    return Color(c.red * k, c.green * k, c.blue * k, c.alpha)
}
