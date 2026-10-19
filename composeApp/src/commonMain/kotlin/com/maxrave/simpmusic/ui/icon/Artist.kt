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
val SimpIcons.Artist: ImageVector
  get() {
    if (_Artist != null) {
      return _Artist!!
    }
    _Artist =
      ImageVector.Builder(
          name = "Artist",
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
            moveTo(15.73f, 19.27f)
            quadTo(15f, 18.55f, 15f, 17.5f)
            reflectiveQuadToRelative(0.73f, -1.78f)
            reflectiveQuadTo(17.5f, 15f)
            quadToRelative(0.2f, 0f, 0.45f, 0.04f)
            reflectiveQuadTo(18.5f, 15.2f)
            verticalLineTo(11f)
            quadToRelative(0f, -0.43f, 0.29f, -0.71f)
            reflectiveQuadTo(19.5f, 10f)
            horizontalLineTo(21f)
            quadToRelative(0.43f, 0f, 0.71f, 0.29f)
            reflectiveQuadTo(22f, 11f)
            reflectiveQuadToRelative(-0.29f, 0.71f)
            reflectiveQuadTo(21f, 12f)
            horizontalLineTo(20f)
            verticalLineToRelative(5.5f)
            quadToRelative(0f, 1.05f, -0.72f, 1.77f)
            reflectiveQuadTo(17.5f, 20f)
            reflectiveQuadTo(15.73f, 19.27f)
            close()
            moveTo(8.18f, 10.83f)
            quadTo(7f, 9.65f, 7f, 8f)
            reflectiveQuadTo(8.18f, 5.18f)
            reflectiveQuadTo(11f, 4f)
            reflectiveQuadToRelative(2.83f, 1.18f)
            reflectiveQuadTo(15f, 8f)
            reflectiveQuadToRelative(-1.17f, 2.82f)
            reflectiveQuadTo(11f, 12f)
            reflectiveQuadTo(8.18f, 10.83f)
            close()
            moveTo(4f, 20f)
            quadTo(3.58f, 20f, 3.29f, 19.71f)
            quadTo(3f, 19.43f, 3f, 19f)
            verticalLineTo(17.2f)
            quadTo(3f, 16.33f, 3.44f, 15.63f)
            reflectiveQuadTo(4.6f, 14.55f)
            quadTo(6.15f, 13.77f, 7.75f, 13.39f)
            reflectiveQuadTo(11f, 13f)
            quadToRelative(0.7f, 0f, 1.39f, 0.07f)
            reflectiveQuadToRelative(1.39f, 0.22f)
            quadToRelative(0.43f, 0.1f, 0.54f, 0.53f)
            reflectiveQuadTo(14.08f, 14.6f)
            quadToRelative(-0.53f, 0.63f, -0.79f, 1.36f)
            reflectiveQuadTo(13.03f, 17.5f)
            quadToRelative(0f, 0.32f, 0.04f, 0.64f)
            reflectiveQuadToRelative(0.14f, 0.64f)
            quadToRelative(0.13f, 0.45f, -0.11f, 0.84f)
            reflectiveQuadTo(12.43f, 20f)
            horizontalLineTo(4f)
            close()
          }
        }
        .buildSimpIcon()
    return _Artist!!
  }

private var _Artist: ImageVector? = null
