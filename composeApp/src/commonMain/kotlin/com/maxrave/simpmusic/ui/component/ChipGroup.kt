package com.maxrave.simpmusic.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maxrave.simpmusic.expect.ui.PlatformBackdrop
import com.maxrave.simpmusic.ui.theme.LocalLiquidGlassEnabled

@Composable
fun Chip(
    isAnimated: Boolean = false,
    isSelected: Boolean = false,
    text: String,
    // A row floating over scrolling content (Home, Library, Search) passes the backdrop its content
    // records into, and its unselected chips turn to liquid glass while the setting is on. The selected
    // chip stays a solid `primary` fill, as in Spotify's row. Chips on a flat card or sheet pass
    // nothing: glass over a flat surface reads as a mere border.
    backdrop: PlatformBackdrop? = null,
    onClick: () -> Unit,
) {
    val glassOn = LocalLiquidGlassEnabled.current
    val glassBackdrop = backdrop?.takeIf { glassOn && !isSelected }
    ChipFrame(isAnimated = isAnimated && isSelected) {
        ChipBody(
            text = text,
            selected = isSelected,
            // The 1dp uniform rim every small glass control here carries: the default rim lights one
            // side only, which on a chip this small does not show at all.
            modifier = if (glassBackdrop != null) Modifier.liquidGlass(glassBackdrop, highlight = Highlight(width = 1.dp)) else Modifier,
            // outlineVariant, per M3's Aug 2024 chip update: a softer stroke than outline.
            // Glass draws its own rim, so the outline goes.
            borderColor = if (glassBackdrop != null) Color.Transparent else MaterialTheme.colorScheme.outlineVariant,
            onClick = onClick,
        )
    }
}

/**
 * The loading ring around a chip. It also pads the chip by its 1dp border width, so anything that has
 * to be the size of a chip goes through here too.
 */
@Composable
private fun ChipFrame(
    isAnimated: Boolean,
    content: @Composable () -> Unit,
) {
    InfiniteBorderAnimationView(
        isAnimated = isAnimated,
        brush = Brush.sweepGradient(listOf(Color.Gray, Color.White)),
        backgroundColor = Color.Transparent,
        contentPadding = 0.dp,
        borderWidth = 1.dp,
        shape = CircleShape,
        oneCircleDurationMillis = 2500,
        content = content,
    )
}

@Composable
private fun ChipBody(
    text: String,
    selected: Boolean,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    // Unselected chips have no fill at all. They mostly sit over artwork (Home) where a solid pill
    // reads as a slab punched over the image; the outline is what carries the shape now. The selected
    // chip deliberately stays opaque — that contrast IS the selection, and it is the only one that has
    // to be found at a glance.
    containerColor: Color = Color.Transparent,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides Dp.Unspecified) {
        ElevatedFilterChip(
            modifier = modifier,
            shape = CircleShape,
            // No shadow. An unselected chip has no fill, and a drop shadow under a see-through
            // shape shows THROUGH it as a dark ring instead of sitting behind it. Naming
            // `elevation` alone is enough: pressed, focused and hovered all default to it.
            elevation = FilterChipDefaults.elevatedFilterChipElevation(elevation = 0.dp),
            colors =
                FilterChipDefaults.elevatedFilterChipColors(
                    containerColor = containerColor,
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    labelColor = labelColor,
                    // `primary` is a pale #B2C5FF on the dark theme, so the label has to be its
                    // matching `on` token — a hand-picked white would read on one theme and
                    // vanish on the other.
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            onClick = onClick,
            label = {
                Text(text, maxLines = 1)
            },
            border =
                FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = selected,
                    selectedBorderColor = Color.Transparent,
                    borderColor = borderColor,
                ),
            // No check icon. A solid `primary` fill against a fully transparent unselected chip
            // is already the strongest contrast available, so a second signal adds nothing — and
            // it cost real usability: the icon made the selected chip ~26dp wider than itself, so
            // every tap reflowed the rest of the scrolling row sideways under the finger that had
            // just tapped it. Material ships the tick because its own default selected fill
            // (`secondaryContainer`) is too faint to stand alone; that is not the case here.
            // Selection also survives without colour vision, being filled-versus-empty rather
            // than one hue against another.
            selected = selected,
        )
    }
}
