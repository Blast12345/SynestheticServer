package color.rgb

import math.normalization.UnitInterval
import math.physics.Light

class ChromaticityFactory {

    fun fromLight(light: Light): Chromaticity? {
        val max = maxOf(light.red, light.green, light.blue)
        if (max == 0.0) return null

        val srgb = LinearRgbColor(
            red = UnitInterval.clamped(light.red / max),
            green = UnitInterval.clamped(light.green / max),
            blue = UnitInterval.clamped(light.blue / max)
        ).toSrgb()

        return srgb.toHsb().chromaticity
    }

}