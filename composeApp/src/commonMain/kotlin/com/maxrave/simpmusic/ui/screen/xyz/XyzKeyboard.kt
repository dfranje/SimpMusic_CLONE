package com.maxrave.simpmusic.ui.screen.xyz

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxrave.simpmusic.extension.LeftToRight
import com.maxrave.simpmusic.ui.icon.Backspace
import com.maxrave.simpmusic.ui.icon.KeyboardReturn
import com.maxrave.simpmusic.ui.icon.Shift
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.theme.typo
import org.jetbrains.compose.resources.stringResource
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.xyz_kb_delete
import simpmusic.composeapp.generated.resources.xyz_kb_enter
import simpmusic.composeapp.generated.resources.xyz_kb_shift
import simpmusic.composeapp.generated.resources.xyz_kb_space

// Every row is laid out on twelve key widths, like the radio's; shorter rows are centred.
private const val ROW_UNITS = 12f
private val KeyGap = 6.dp

/**
 * The app's own keyboard, the layout XYZ Radio uses on the head unit (radio-core KeyboardView.buildText):
 * digits, QWERTZ with Š Đ Č Ć Ž, shift, a wide space and Enter. Head units often have no usable system
 * keyboard, so the XYZ shell's search fields only ever use this one. Holding Delete clears everything.
 * Always left to right: it is a key layout, not text.
 */
@Composable
internal fun XyzKeyboard(
    onChar: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var caps by remember { mutableStateOf(false) }
    val type: (Char) -> Unit = { c -> onChar(if (caps) c.uppercase() else c.lowercase()) }
    LeftToRight {
        Column(modifier, verticalArrangement = Arrangement.spacedBy(KeyGap)) {
            KeyRow(units = 10f) { "1234567890".forEach { c -> CharKey(c, caps, onType = type) } }
            KeyRow(units = 12f) { "QWERTZUIOPŠĐ".forEach { c -> CharKey(c, caps, onType = type) } }
            KeyRow(units = 12f) { "ASDFGHJKLČĆŽ".forEach { c -> CharKey(c, caps, onType = type) } }
            KeyRow(units = 11f) {
                IconKey(
                    SimpIcons.Shift,
                    stringResource(Res.string.xyz_kb_shift),
                    weight = 2f,
                    highlighted = caps,
                    onClick = { caps = !caps },
                )
                "YXCVBNM".forEach { c -> CharKey(c, caps, onType = type) }
                IconKey(
                    SimpIcons.Backspace,
                    stringResource(Res.string.xyz_kb_delete),
                    weight = 2f,
                    onClick = onBackspace,
                    onLongClick = onClear,
                )
            }
            KeyRow(units = 12f) {
                Key(weight = 9f, label = stringResource(Res.string.xyz_kb_space), onClick = { onChar(" ") }) {
                    Text(stringResource(Res.string.xyz_kb_space), style = typo().bodyMedium, color = xyzPalette().textDim)
                }
                IconKey(SimpIcons.KeyboardReturn, stringResource(Res.string.xyz_kb_enter), weight = 3f, accent = true, onClick = onDone)
            }
        }
    }
}

/** One row of keys, [units] wide out of twelve, centred. */
@Composable
private fun androidx.compose.foundation.layout.ColumnScope.KeyRow(
    units: Float,
    keys: @Composable RowScope.() -> Unit,
) {
    Row(
        Modifier
            .weight(1f)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(KeyGap),
    ) {
        val side = (ROW_UNITS - units) / 2f
        if (side > 0f) Spacer(Modifier.weight(side))
        keys()
        if (side > 0f) Spacer(Modifier.weight(side))
    }
}

@Composable
private fun RowScope.CharKey(
    c: Char,
    caps: Boolean,
    onType: (Char) -> Unit,
) {
    val shown = if (caps || !c.isLetter()) c.uppercase() else c.lowercase()
    Key(weight = 1f, label = shown, onClick = { onType(c) }) {
        Text(shown, style = typo().titleMedium.copy(fontSize = 21.sp), color = xyzPalette().text)
    }
}

@Composable
private fun RowScope.IconKey(
    icon: ImageVector,
    label: String,
    weight: Float,
    highlighted: Boolean = false,
    accent: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    val p = xyzPalette()
    Key(weight = weight, label = label, highlighted = highlighted, accent = accent, onClick = onClick, onLongClick = onLongClick) {
        Icon(
            icon,
            contentDescription = null,
            tint = if (accent) MaterialTheme.colorScheme.onPrimary else p.text,
            modifier = Modifier.size(24.dp),
        )
    }
}

/** A key tile in the launcher's tile colour; Enter in the accent, a held Shift tinted by it. */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RowScope.Key(
    weight: Float,
    label: String,
    highlighted: Boolean = false,
    accent: Boolean = false,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val p = xyzPalette()
    val shape = RoundedCornerShape(p.corner(10.dp))
    val fill =
        when {
            accent -> p.accent
            highlighted -> p.accent.copy(alpha = 0.35f)
            else -> p.tile
        }
    Box(
        Modifier
            .weight(weight)
            .fillMaxHeight()
            .clip(shape)
            .background(if (fill == Color.Unspecified) p.tile else fill, shape)
            .combinedClickable(
                onClickLabel = label,
                role = Role.Button,
                onLongClick = onLongClick,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}
