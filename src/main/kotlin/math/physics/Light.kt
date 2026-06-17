package math.physics

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

//    val chromaticity: Chromaticity? by lazy {
//        if (radiantFlux == 0.0) return@lazy null
//
//        val max = maxOf(red, green, blue)
//        val min = minOf(red, green, blue)
//        val chroma = max - min
//
//        if (chroma == 0.0) {
//            return@lazy Chromaticity.Achromatic
//        }
//
//        val hue = Angle.fromDegrees(
//            when (max) {
//                red -> 60.0 * ((green - blue) / chroma).mod(6.0)
//                green -> 60.0 * ((blue - red) / chroma + 2.0)
//                else -> 60.0 * ((red - green) / chroma + 4.0)
//            }
//        )
//
//        val saturation = UnitInterval.clamped(chroma / max)
//
//        return@lazy Chromaticity.Chromatic(hue, saturation)
//    }

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