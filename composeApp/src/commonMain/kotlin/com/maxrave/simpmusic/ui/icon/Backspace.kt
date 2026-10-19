package com.maxrave.simpmusic.ui.icon

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val SimpIcons.Backspace: ImageVector
  get() {
    if (_Backspace != null) {
      return _Backspace!!
    }
    _Backspace =
      ImageVector.Builder(
          name = "Backspace",
          defaultWidth = 24.dp,
          defaultHeight = 24.dp,
          viewportWidth = 24f,
          viewportHeight = 24f,
        )
        .apply {
          path(
            fill = SolidColor(Color.Black),
            fillAlpha = 1f,
            stroke = null,
            strokeAlpha = 1f,
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Butt,
            strokeLineJoin = StrokeJoin.Bevel,
            strokeLineMiter = 1f,
            pathFillType = PathFillType.Companion.NonZero,
          ) {
            moveTo(14f, 13.4f)
            lineToRelative(1.9f, 1.9f)
            quadToRelative(0.28f, 0.28f, 0.7f, 0.28f)
            reflectiveQuadTo(17.3f, 15.3f)
            quadToRelative(0.27f, -0.27f, 0.27f, -0.7f)
            reflectiveQuadTo(17.3f, 13.9f)
            lineTo(15.4f, 12f)
            lineToRelative(1.9f, -1.9f)
            quadTo(17.58f, 9.82f, 17.58f, 9.4f)
            quadToRelative(0f, -0.42f, -0.27f, -0.7f)
            reflectiveQuadTo(16.6f, 8.42f)
            reflectiveQuadTo(15.9f, 8.7f)
            lineTo(14f, 10.6f)
            lineTo(12.1f, 8.7f)
            quadTo(11.83f, 8.42f, 11.4f, 8.42f)
            reflectiveQuadTo(10.7f, 8.7f)
            reflectiveQuadTo(10.43f, 9.4f)
            reflectiveQuadToRelative(0.28f, 0.7f)
            lineTo(12.6f, 12f)
            lineToRelative(-1.9f, 1.9f)
            quadToRelative(-0.28f, 0.28f, -0.28f, 0.7f)
            reflectiveQuadToRelative(0.28f, 0.7f)
            quadToRelative(0.28f, 0.28f, 0.7f, 0.28f)
            reflectiveQuadTo(12.1f, 15.3f)
            lineTo(14f, 13.4f)
            close()
            moveTo(9f, 20f)
            quadTo(8.53f, 20f, 8.1f, 19.79f)
            quadTo(7.68f, 19.58f, 7.4f, 19.2f)
            lineToRelative(-4.5f, -6f)
            quadTo(2.5f, 12.68f, 2.5f, 12f)
            reflectiveQuadTo(2.9f, 10.8f)
            lineToRelative(4.5f, -6f)
            quadTo(7.68f, 4.42f, 8.1f, 4.21f)
            quadTo(8.53f, 4f, 9f, 4f)
            horizontalLineTo(20f)
            quadToRelative(0.83f, 0f, 1.41f, 0.59f)
            quadTo(22f, 5.18f, 22f, 6f)
            verticalLineTo(18f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(20f, 20f)
            horizontalLineTo(9f)
            close()
          }
        }
        .buildSimpIcon()
    return _Backspace!!
  }

private var _Backspace: ImageVector? = null
