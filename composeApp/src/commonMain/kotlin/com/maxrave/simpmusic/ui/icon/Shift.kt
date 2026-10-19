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
val SimpIcons.Shift: ImageVector
  get() {
    if (_Shift != null) {
      return _Shift!!
    }
    _Shift =
      ImageVector.Builder(
          name = "Shift",
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
            moveTo(8f, 20f)
            verticalLineTo(13f)
            horizontalLineTo(5.1f)
            quadTo(4.45f, 13f, 4.19f, 12.44f)
            reflectiveQuadTo(4.33f, 11.38f)
            lineToRelative(6.9f, -8.43f)
            quadTo(11.53f, 2.57f, 12f, 2.57f)
            reflectiveQuadToRelative(0.78f, 0.38f)
            lineToRelative(6.9f, 8.43f)
            quadToRelative(0.4f, 0.5f, 0.14f, 1.06f)
            reflectiveQuadTo(18.9f, 13f)
            horizontalLineTo(16f)
            verticalLineToRelative(7f)
            quadToRelative(0f, 0.43f, -0.29f, 0.71f)
            reflectiveQuadTo(15f, 21f)
            horizontalLineTo(9f)
            quadTo(8.58f, 21f, 8.29f, 20.71f)
            quadTo(8f, 20.43f, 8f, 20f)
            close()
          }
        }
        .buildSimpIcon()
    return _Shift!!
  }

private var _Shift: ImageVector? = null
