package com.mindset.icons

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mindset.MindSetIcons

val MindSetIcons.WallBall: ImageVector
    get() {
        if (_WallBall != null) {
            return _WallBall!!
        }
        _WallBall = ImageVector.Builder(
            name = "WallBall",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            path(
                fill = SolidColor(Color.White),
                pathFillType = PathFillType.EvenOdd
            ) {
                moveTo(12f, 2f)
                arcTo(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 22f)
                arcTo(10f, 10f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 2f)
                close()
                moveTo(12f, 4.5f)
                arcTo(7.5f, 7.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 19.5f)
                arcTo(7.5f, 7.5f, 0f, isMoreThanHalf = true, isPositiveArc = true, 12f, 4.5f)
                close()
                moveTo(12f, 9f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 15f)
                arcTo(3f, 3f, 0f, isMoreThanHalf = true, isPositiveArc = false, 12f, 9f)
                close()
            }
        }.build()

        return _WallBall!!
    }

@Suppress("ObjectPropertyName")
private var _WallBall: ImageVector? = null

@Preview
@Composable
private fun WallBallPreview() {
    Box(modifier = Modifier.padding(12.dp)) {
        Image(imageVector = MindSetIcons.WallBall, contentDescription = null)
    }
}
