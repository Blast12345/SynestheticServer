package color.rgb

import color.RgbColorSpace
import math.normalization.UnitInterval

data class RgbColor<S : RgbColorSpace>(
    val red: UnitInterval,
    val green: UnitInterval,
    val blue: UnitInterval,
)

// Helpers
inline fun <S : RgbColorSpace, T : RgbColorSpace> RgbColor<S>.mapChannels(
    transform: (Double) -> Double
): RgbColor<T> = RgbColor(
    red = UnitInterval(transform(red.value)),
    green = UnitInterval(transform(green.value)),
    blue = UnitInterval(transform(blue.value)),
)