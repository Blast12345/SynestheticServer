package gui.extensions

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope

fun DrawScope.cartesian(x: Float, y: Float): Offset = Offset(x, size.height - y)