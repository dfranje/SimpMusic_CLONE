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
val SimpIcons.KeyboardReturn: ImageVector
  get() {
    if (_KeyboardReturn != null) {
      return _KeyboardReturn!!
    }
    _KeyboardReturn =
      ImageVector.Builder(
          name = "KeyboardReturn",
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
            moveTo(6.8f, 13f)
            lineToRelative(2.9f, 2.9f)
            quadToRelative(0.28f, 0.28f, 0.28f, 0.7f)
            quadToRelative(0f, 0.42f, -0.28f, 0.7f)
            reflectiveQuadTo(9f, 17.58f)
            quadToRelative(-0.42f, 0f, -0.7f, -0.27f)
            lineTo(3.7f, 12.7f)
            quadTo(3.55f, 12.55f, 3.49f, 12.38f)
            reflectiveQuadTo(3.43f, 12f)
            reflectiveQuadTo(3.49f, 11.63f)
            reflectiveQuadTo(3.7f, 11.3f)
            lineTo(8.3f, 6.7f)
            quadTo(8.58f, 6.43f, 9f, 6.43f)
            quadToRelative(0.43f, 0f, 0.7f, 0.27f)
            reflectiveQuadTo(9.98f, 7.4f)
            reflectiveQuadTo(9.7f, 8.1f)
            lineTo(6.8f, 11f)
            horizontalLineTo(19f)
            verticalLineTo(8f)
            quadTo(19f, 7.57f, 19.29f, 7.29f)
            reflectiveQuadTo(20f, 7f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(21f, 8f)
            verticalLineToRelative(3f)
            quadToRelative(0f, 0.82f, -0.59f, 1.41f)
            reflectiveQuadTo(19f, 13f)
            horizontalLineTo(6.8f)
            close()
          }
        }
        .buildSimpIcon()
    return _KeyboardReturn!!
  }

private var _KeyboardReturn: ImageVector? = null
