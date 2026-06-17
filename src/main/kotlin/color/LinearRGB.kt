package color

import kotlin.math.pow

typealias LinearRgbColor = RgbColor<LinearRGB>

fun LinearRgbColor.toSrgb(): StandardRgbColor = mapChannels { value ->
    if (value <= 0.0031308) value * 12.92
    else 1.055 * value.pow(1.0 / 2.4) - 0.055
}
