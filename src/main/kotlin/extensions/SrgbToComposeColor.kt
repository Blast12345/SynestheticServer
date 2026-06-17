package extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import annotations.SkipCoverage
import color.rgb.StandardRgbColor

@SkipCoverage
fun StandardRgbColor.toComposeColor(): Color = Color(
    red = red.value.toFloat(),
    green = green.value.toFloat(),
    blue = blue.value.toFloat(),
    colorSpace = ColorSpaces.Srgb
)