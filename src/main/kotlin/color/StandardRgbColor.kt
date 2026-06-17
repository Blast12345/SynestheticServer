package color

import math.geometry.Angle
import math.normalization.UnitInterval
import kotlin.math.pow

typealias StandardRgbColor = RgbColor<StandardRGB>

object StandardRgbColors {
    val Black = StandardRgbColor(UnitInterval.zero, UnitInterval.zero, UnitInterval.zero)
    val White = StandardRgbColor(UnitInterval.one, UnitInterval.one, UnitInterval.one)
}

fun StandardRgbColor.toLinear(): LinearRgbColor = mapChannels { value ->
    if (value <= 0.04045) value / 12.92
    else ((value + 0.055) / 1.055).pow(2.4)
}

fun StandardRgbColor.toHsb(): HsbColor<StandardRGB> {
    val r = red.value
    val g = green.value
    val b = blue.value

    val max = maxOf(r, g, b)
    val min = minOf(r, g, b)
    val chroma = max - min

    val hue = if (chroma == 0.0) Angle.zero else Angle.fromDegrees(
        when (max) {
            r -> 60.0 * ((g - b) / chroma).mod(6.0)
            g -> 60.0 * ((b - r) / chroma + 2.0)
            else -> 60.0 * ((r - g) / chroma + 4.0)
        }
    )

    val saturation = if (max == 0.0) 0.0 else chroma / max
    val brightness = max

    return HsbColor(
        hue = hue,
        saturation = UnitInterval.clamped(saturation),
        brightness = UnitInterval.clamped(brightness)
    )
}