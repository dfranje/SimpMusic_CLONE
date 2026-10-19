package com.maxrave.simpmusic.ui.screen.xyz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.maxrave.simpmusic.ui.component.rememberHolderPainter
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.LocalXyzPalette
import com.maxrave.simpmusic.ui.theme.XyzPalette
import com.maxrave.simpmusic.ui.theme.buildXyzPalette
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.xyzRim
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.xyz_clear_search

/**
 * The palette the XYZ shell draws with. The shell is only composed while the XYZ look is on, so the
 * fallback is there for previews and for a theme switch caught mid-frame, not as a second look.
 */
@Composable
internal fun xyzPalette(): XyzPalette = LocalXyzPalette.current ?: buildXyzPalette(0, dark = true)

/** One of the launcher's cards: its gradient fill, accent rim and glow. */
@Composable
internal fun XyzCard(
    modifier: Modifier = Modifier,
    corner: Dp = 22.dp,
    glow: Boolean = true,
    content: @Composable BoxScope.() -> Unit,
) {
    val p = xyzPalette()
    val radius = p.corner(corner)
    val shape = RoundedCornerShape(radius)
    Box(
        modifier =
            modifier
                .xyzRim(radius, shape, glow = glow)
                .clip(shape)
                .background(p.cardBrush, shape),
        content = content,
    )
}

/** The launcher's search pill: a magnifier, the field and a clear button once something is typed. */
@Composable
internal fun XyzSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = xyzPalette()
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(50)
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .height(52.dp)
                .xyzRim(26.dp, shape, glow = false)
                .clip(shape)
                .background(p.cardBrush, shape)
                .padding(start = 18.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(SimpIcons.Search, contentDescription = null, tint = p.textDim, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(12.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = typo().bodyLarge.copy(color = p.text),
            cursorBrush = SolidColor(p.accent),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions =
                KeyboardActions(
                    onSearch = {
                        onSearch()
                        focusManager.clearFocus()
                    },
                ),
            modifier = Modifier.weight(1f),
            decorationBox = { field ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(placeholder, style = typo().bodyLarge, color = p.textDim, maxLines = 1)
                    }
                    field()
                }
            },
        )
        if (query.isNotEmpty()) {
            IconButton(onClick = { onQueryChange("") }) {
                Icon(SimpIcons.Close, contentDescription = stringResource(Res.string.xyz_clear_search), tint = p.textDim)
            }
        } else {
            Spacer(Modifier.width(12.dp))
        }
    }
}

/** A section title in the launcher's style, with optional actions at the end of the row. */
@Composable
internal fun XyzSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    actions: @Composable () -> Unit = {},
) {
    val p = xyzPalette()
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, style = typo().titleMedium, color = p.text, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) {
                Text(subtitle, style = typo().bodyMedium, color = p.textDim, maxLines = 1)
            }
        }
        actions()
    }
}

/** The launcher's pill buttons: accent-filled ("Play all") or rimmed ("Shuffle"). */
@Composable
internal fun XyzPillButton(
    text: String,
    filled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
) {
    val p = xyzPalette()
    val shape = RoundedCornerShape(14.dp)
    val content = if (filled) MaterialTheme.colorScheme.onPrimary else p.text
    Row(
        modifier =
            modifier
                .height(44.dp)
                .then(if (filled) Modifier else Modifier.xyzRim(14.dp, shape, glow = false))
                .clip(shape)
                .background(
                    if (filled) Brush.horizontalGradient(listOf(p.accent, lerp(p.accent, p.accentDeep, 0.35f))) else p.cardBrush,
                    shape,
                ).clickable(onClick = onClick)
                .padding(PaddingValues(horizontal = 18.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (icon != null) Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(20.dp))
        Text(text, style = typo().labelMedium, color = content, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}

/** Artwork in a rounded frame on the launcher's tile colour, with the app's placeholder while it loads. */
@Composable
internal fun XyzArtwork(
    url: String?,
    modifier: Modifier = Modifier,
    corner: Dp = 12.dp,
    round: Boolean = false,
) {
    val p = xyzPalette()
    val shape = if (round) CircleShape else RoundedCornerShape(p.corner(corner))
    AsyncImage(
        model =
            ImageRequest
                .Builder(LocalPlatformContext.current)
                .data(url)
                .crossfade(true)
                .build(),
        placeholder = rememberHolderPainter(),
        error = rememberHolderPainter(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(shape).background(p.tile, shape),
    )
}

/** A centred line for empty lists and search states. */
@Composable
internal fun XyzEmptyText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = xyzPalette().textDim,
) {
    Box(modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
        Text(text, style = typo().bodyMedium, color = color)
    }
}
