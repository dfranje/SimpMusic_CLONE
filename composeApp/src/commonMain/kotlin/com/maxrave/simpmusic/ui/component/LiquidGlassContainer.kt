package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop

/**
 * The rim a glass surface draws. It used to be Kyant's own Highlight; with the library gone it is
 * a thin light border on the flat surface, so call sites keep passing the width they chose.
 */
@Immutable
class Highlight(
    val width: Dp = 0.5.dp,
    val alpha: Float = 1f,
) {
    companion object {
        val Default = Highlight()
    }
}

/**
 * The surface every "glass" element draws: the flat translucent pill the detail screens used
 * before liquid glass (surfaceContainerHighest @ 80%), plus a faint rim. The XYZ build has no
 * glass; the name and parameters stay so the call sites did not have to change.
 */
@Composable
fun Modifier.liquidGlass(
    @Suppress("UNUSED_PARAMETER") backdrop: PlatformBackdrop,
    shape: Shape = CircleShape,
    @Suppress("UNUSED_PARAMETER") interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
): Modifier =
    this
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.8f))
        .border(highlight.width, Color.White.copy(alpha = 0.12f * highlight.alpha), shape)

/**
 * The overload the MiniPlayer and the Desktop capsule used to sample their own background with.
 * Drawn flat like the one above; [layer], [luminance] and the scrim values no longer do anything.
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun Modifier.liquidGlass(
    backdrop: PlatformBackdrop,
    layer: GraphicsLayer,
    luminance: State<Float>,
    shape: Shape = CircleShape,
    interactive: Boolean = true,
    blurScale: Float = 1f,
    minScrim: Float = 0.12f,
    maxScrim: Float = 0.5f,
): Modifier =
    this
        .clip(shape)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f))

/**
 * A liquid-glass surface wrapping arbitrary [content] (e.g. a pill of icon
 * buttons). Thin convenience over [liquidGlass].
 */
@Composable
fun LiquidGlassContainer(
    backdrop: PlatformBackdrop,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    contentAlignment: Alignment = Alignment.Center,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier.liquidGlass(backdrop, shape, interactive, highlight),
        contentAlignment = contentAlignment,
        content = content,
    )
}

/**
 * Convenience wrapper around [LiquidGlassContainer] for the common single-icon
 * case (e.g. the circular back button shared by the detail screens).
 */
@Composable
fun LiquidGlassIconButton(
    backdrop: PlatformBackdrop,
    imageVector: ImageVector,
    modifier: Modifier = Modifier.size(48.dp),
    shape: Shape = CircleShape,
    tint: Color = Color.White,
    interactive: Boolean = true,
    highlight: Highlight = Highlight.Default,
    onClick: () -> Unit,
) {
    LiquidGlassContainer(
        backdrop = backdrop,
        modifier = modifier,
        shape = shape,
        interactive = interactive,
        highlight = highlight,
    ) {
        RippleIconButton(
            imageVector = imageVector,
            tint = tint,
            onClick = onClick,
        )
    }
}
