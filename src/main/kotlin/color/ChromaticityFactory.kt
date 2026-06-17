package color

import math.geometry.Angle
import math.normalization.UnitInterval
import math.physics.Light

class ChromaticityFactory {

    fun fromLight(light: Light): Chromaticity? {
        val foobar = maxOf(light.red, light.green, light.blue)

        if (foobar == 0.0) return null

        val srgb = LinearRgbColor(
            red = UnitInterval.clamped(light.red / foobar),
            green = UnitInterval.clamped(light.green / foobar),
            blue = UnitInterval.clamped(light.blue / foobar)
        ).toSrgb()

        val red = srgb.red.value
        val green = srgb.green.value
        val blue = srgb.blue.value

        val max = maxOf(red, green, blue)
        val min = minOf(red, green, blue)
        val chroma = max - min

        if (chroma == 0.0) {
            return Chromaticity.Achromatic
        }

        val hue = Angle.fromDegrees(
            when (max) {
                red -> 60.0 * ((green - blue) / chroma).mod(6.0)
                green -> 60.0 * ((blue - red) / chroma + 2.0)
                else -> 60.0 * ((red - green) / chroma + 4.0)
            }
        )

        val saturation = UnitInterval.clamped(chroma / max)

        return Chromaticity.Chromatic(hue, saturation)
    }

}