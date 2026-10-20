package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.simpmusic.expect.ui.toImageBitmap
import com.maxrave.simpmusic.extension.rgbFactor
import com.maxrave.simpmusic.extension.smoothScrimBrush

private val paddingMedium = 0.dp

private val titlePaddingStart = 20.dp
private val titlePaddingEnd = 72.dp

private const val TITLE_FONT_SCALE_START = 1f
private const val TITLE_FONT_SCALE_END = 0.46f

@Composable
private fun Header(
    modifier: Modifier = Modifier,
    scroll: ScrollState,
    imageUrl: String? = null,
    headerHeightPx: Float,
    backgroundColor: Color,
    onImageLoaded: (ImageBitmap) -> Unit,
) {
    Box(
        modifier =
            modifier
                .graphicsLayer {
                    translationY = -scroll.value.toFloat() / 2f // Parallax effect
                    alpha = (-1f / headerHeightPx) * scroll.value + 1
                }.background(
                    backgroundColor.rgbFactor(0.5f),
                ),
    ) {
        AsyncImage(
            model =
                ImageRequest
                    .Builder(LocalPlatformContext.current)
                    .data(imageUrl)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .diskCacheKey(imageUrl)
                    .crossfade(true)
                    .build(),
            onSuccess = {
                onImageLoaded(
                    it.result.image.toImageBitmap(),
                )
            },
            placeholder = rememberHolderPainter(isVideo = true),
            error = rememberHolderPainter(isVideo = true),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier =
                Modifier
                    .fillMaxSize(),
        )
        // YouTube Music style gradient - smooth fade from image to content
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    brush =
                        smoothScrimBrush(
                            from = Color.Black.copy(alpha = 0f),
                            to = Color.Black,
                            startY = headerHeightPx / 2, // Start fade at middle of header
                            endY = headerHeightPx, // Complete at bottom of header
                        ),
                ),
        )
    }
}

@Composable
private fun Title(
    scroll: ScrollState,
    modifier: Modifier = Modifier,
    headerHeight: Dp,
    toolbarHeight: Dp,
    title: String,
) {
    var titleHeightPx by remember { mutableFloatStateOf(0f) }
    var titleWidthPx by remember { mutableFloatStateOf(0f) }

    Text(
        text = title,
        fontSize = 30.sp, // Reduced from 48.sp — still bold and prominent, fits longer names
        fontWeight = FontWeight.Bold,
        color = Color.White,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
            modifier
                .graphicsLayer {
                    val collapseRange: Float = (headerHeight.toPx() - toolbarHeight.toPx())
                    val collapseFraction: Float = (scroll.value / collapseRange).coerceIn(0f, 1f)

                    val scaleXY =
                        lerp(
                            TITLE_FONT_SCALE_START.dp,
                            TITLE_FONT_SCALE_END.dp,
                            collapseFraction,
                        )

                    val titleExtraStartPadding = titleWidthPx.toDp() * (1 - scaleXY.value) / 2f

                    val titleYFirstInterpolatedPoint =
                        lerp(
                            headerHeight - titleHeightPx.toDp() - paddingMedium,
                            headerHeight / 2,
                            collapseFraction,
                        )

                    val titleXFirstInterpolatedPoint =
                        lerp(
                            titlePaddingStart,
                            (titlePaddingEnd - titleExtraStartPadding) * 5 / 4,
                            collapseFraction,
                        )

                    val titleYSecondInterpolatedPoint =
                        lerp(
                            headerHeight / 2,
                            toolbarHeight / 2 - titleHeightPx.toDp() / 2,
                            collapseFraction,
                        )

                    val titleXSecondInterpolatedPoint =
                        lerp(
                            (titlePaddingEnd - titleExtraStartPadding) * 5 / 4,
                            titlePaddingEnd - titleExtraStartPadding,
                            collapseFraction,
                        )

                    val titleY =
                        lerp(
                            titleYFirstInterpolatedPoint,
                            titleYSecondInterpolatedPoint,
                            collapseFraction,
                        )

                    val titleX =
                        lerp(
                            titleXFirstInterpolatedPoint,
                            titleXSecondInterpolatedPoint,
                            collapseFraction,
                        )

                    translationY = titleY.toPx()
                    translationX = titleX.toPx()
                    scaleX = scaleXY.value
                    scaleY = scaleXY.value
                }.onGloballyPositioned {
                    titleHeightPx = it.size.height.toFloat()
                    titleWidthPx = it.size.width.toFloat()
                },
    )
}
