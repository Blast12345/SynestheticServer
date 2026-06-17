package color

import math.geometry.Angle
import math.normalization.UnitInterval
import kotlin.math.abs

// NOTE: As I understand it, HSB and HSV are the same. I use HSB because "brightness" is more intuitive than "value"
data class HsbColor<S : RgbColorSpace>(
    val hue: Angle,
    val saturation: UnitInterval,
    val brightness: UnitInterval
) {

    val chromaticity: Chromaticity
        get() = if (saturation == UnitInterval.zero) Chromaticity.Achromatic
        else Chromaticity.Chromatic(hue, saturation)

    companion object {
        fun <S : RgbColorSpace> from(
            chromaticity: Chromaticity,
            brightness: UnitInterval
        ): HsbColor<S> = when (chromaticity) {
            // achromatic light has no hue; choosing zero for sake of simplicity
            is Chromaticity.Achromatic -> HsbColor(Angle.zero, UnitInterval.zero, brightness)
            is Chromaticity.Chromatic -> HsbColor(chromaticity.hue, chromaticity.saturation, brightness)
        }
    }

    fun toRgb(): RgbColor<S> {
        val chroma = brightness.value * saturation.value
        val hueSegment = hue.degrees / 60.0
        val secondary = chroma * (1.0 - abs(hueSegment % 2.0 - 1.0))
        val match = brightness.value - chroma

        val (red, green, blue) = when {
            hueSegment < 1.0 -> Triple(chroma, secondary, 0.0)
            hueSegment < 2.0 -> Triple(secondary, chroma, 0.0)
            hueSegment < 3.0 -> Triple(0.0, chroma, secondary)
            hueSegment < 4.0 -> Triple(0.0, secondary, chroma)
            hueSegment < 5.0 -> Triple(secondary, 0.0, chroma)
            else -> Triple(chroma, 0.0, secondary)
        }

        return RgbColor(
            red = UnitInterval(red + match),
            green = UnitInterval(green + match),
            blue = UnitInterval(blue + match),
        )
    }

}