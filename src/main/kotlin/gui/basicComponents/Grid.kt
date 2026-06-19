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
import kotlin.math.round

@Composable
fun Grid(content: @Composable () -> Unit) {
    val lineColor = MaterialTheme.colors.primary

    Box(modifier = Modifier.border(1.dp, lineColor)) {
        Canvas(modifier = Modifier.fillMaxSize()) { drawGrid(lineColor) }
        content()
    }
}

private fun DrawScope.drawGrid(
    lineColor: Color,
    horizontalLineCount: Int = 10,
    verticalLineCount: Int = 10,
) {
    drawHorizontalLines(lineColor, horizontalLineCount)
    drawVerticalLines(lineColor, verticalLineCount)
}

private fun DrawScope.drawHorizontalLines(lineColor: Color, lineCount: Int) {
    val lineInterval = size.height / lineCount
    val strokeWidth = 1f
    val halfStroke = strokeWidth / 2f

    repeat(lineCount) { index ->
        val y = index * lineInterval
        
        val snappedY = round(y - halfStroke) + halfStroke

        drawLine(
            color = lineColor,
            start = Offset(0f, snappedY),
            end = Offset(size.width, snappedY),
            strokeWidth = strokeWidth
        )
    }
}

private fun DrawScope.drawVerticalLines(lineColor: Color, lineCount: Int) {
    val lineInterval = size.width / lineCount
    val strokeWidth = 1f
    val halfStroke = strokeWidth / 2f

    repeat(lineCount) { index ->
        val x = index * lineInterval

        val snappedX = round(x - halfStroke) + halfStroke

        drawLine(
            color = lineColor,
            start = Offset(snappedX, 0f),
            end = Offset(snappedX, size.height),
            strokeWidth = strokeWidth
        )
    }
}