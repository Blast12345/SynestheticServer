package color

import math.normalization.UnitInterval
import kotlin.math.pow

typealias LinearRgbColor = RgbColor<LinearRGB>

object LinearRgbColors {
    val Black = StandardRgbColor(UnitInterval.zero, UnitInterval.zero, UnitInterval.zero)
    val White = StandardRgbColor(UnitInterval.one, UnitInterval.one, UnitInterval.one)
}

fun LinearRgbColor.toSrgb(): StandardRgbColor = mapChannels { value ->
    if (value <= 0.0031308) value * 12.92
    else 1.055 * value.pow(1.0 / 2.4) - 0.055
}
