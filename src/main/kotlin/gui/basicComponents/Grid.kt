package gui.basicComponents

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp

@Composable
fun Grid(content: @Composable () -> Unit) {
    val lineColor = MaterialTheme.colors.primary

    Box(modifier = Modifier.border(1.dp, lineColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawGrid(this, lineColor) }
        content()
    }
}

private fun drawGrid(
    scope: DrawScope,
    lineColor: Color,
    horizontalLineCount: Int = 10,
    verticalLineCount: Int = 10,
) {
    drawHorizontalLines(scope, lineColor, horizontalLineCount)
    drawVerticalLines(scope, lineColor, verticalLineCount)
}

private fun drawHorizontalLines(
    scope: DrawScope,
    lineColor: Color,
    lineCount: Int
) {
    val lineInterval = scope.size.height / lineCount

    repeat(lineCount) { index ->
        val y = index * lineInterval

        scope.drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(scope.size.width, y)
        )
    }
}

private fun drawVerticalLines(
    scope: DrawScope,
    lineColor: Color,
    lineCount: Int
) {
    val lineInterval = scope.size.width / lineCount

    repeat(lineCount) { index ->
        val x = index * lineInterval

        scope.drawLine(
            color = lineColor,
            start = Offset(x, 0f),
            end = Offset(x, scope.size.height)
        )
    }
}
