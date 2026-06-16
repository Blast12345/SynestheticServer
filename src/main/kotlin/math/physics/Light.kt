package math.physics

import color.Chromaticity
import math.geometry.Angle
import math.normalization.UnitInterval
import kotlin.math.pow

// This is a modeling of how light works in the real world (i.e. not based on human perception).
// It is measured in terms of radiant flux, of which the SI unit is watts.
// Light mixing is inherently linear. 500 watts + 500 watts = 1000 watts
// And like light in the real world, the brightness is not capped, so it can be any arbitrarily large size.
data class Light(
    val red: Double = 0.0,
    val green: Double = 0.0,
    val blue: Double = 0.0
) {

    val radiantFlux = red + green + blue

    val chromaticity: Chromaticity? by lazy {
        if (radiantFlux == 0.0) return@lazy null

        fun linearToSrgb(v: Double) =
            if (v <= 0.0031308) 12.92 * v
            else 1.055 * v.pow(1.0 / 2.4) - 0.055

        val r = linearToSrgb(red)
        val g = linearToSrgb(green)
        val b = linearToSrgb(blue)

        val max = maxOf(r, g, b)
        val min = minOf(r, g, b)
        val chroma = max - min

        if (chroma == 0.0) return@lazy Chromaticity.Achromatic

        val hue = Angle.fromDegrees(
            when (max) {
                r -> 60.0 * ((g - b) / chroma).mod(6.0)
                g -> 60.0 * ((b - r) / chroma + 2.0)
                else -> 60.0 * ((r - g) / chroma + 4.0)
            }
        )

        val saturation = UnitInterval.clamped(chroma / max)

        Chromaticity.Chromatic(hue, saturation)
    }

    fun linearToSrgb(value: Double): Double =
        if (value <= 0.0031308) 12.92 * value
        else 1.055 * value.pow(1.0 / 2.4) - 0.055

    operator fun plus(other: Light) = Light(
        red = red + other.red,
        green = green + other.green,
        blue = blue + other.blue
    )

    operator fun times(factor: Double) = Light(
        red = red * factor,
        green = green * factor,
        blue = blue * factor
    )

}