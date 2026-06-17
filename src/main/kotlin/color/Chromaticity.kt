package color

import math.geometry.Angle
import math.normalization.UnitInterval

// Reference: https://en.wikipedia.org/wiki/Chromaticity
sealed class Chromaticity {

    data class Chromatic(
        val hue: Angle,
        val saturation: UnitInterval
    ) : Chromaticity() {
        init {
            require(saturation != UnitInterval.zero) { "Chromatic requires nonzero saturation" }
        }
    }

    data object Achromatic : Chromaticity()

}