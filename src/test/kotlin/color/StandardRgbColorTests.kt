package color

import color.rgb.*
import math.normalization.UnitInterval
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class StandardRgbColorTests {

    @Nested
    inner class ToLinear {

        @Test
        fun `low value uses linear segment`() {
            val srgb = StandardRgbColor(
                red = UnitInterval(0.02),
                green = UnitInterval(0.02),
                blue = UnitInterval(0.02),
            )

            val linear = srgb.toLinear()

            assertRgbEquals(
                expected = LinearRgbColor(
                    red = UnitInterval(0.001547988),
                    green = UnitInterval(0.001547988),
                    blue = UnitInterval(0.001547988),
                ),
                actual = linear,
            )
        }

        @Test
        fun `mid value uses gamma curve`() {
            val srgb = StandardRgbColor(
                red = UnitInterval(0.5),
                green = UnitInterval(0.5),
                blue = UnitInterval(0.5),
            )

            val linear = srgb.toLinear()

            assertRgbEquals(
                expected = LinearRgbColor(
                    red = UnitInterval(0.214041140),
                    green = UnitInterval(0.214041140),
                    blue = UnitInterval(0.214041140),
                ),
                actual = linear,
            )
        }

        @Test
        fun `converting and back yields the original color`() {
            val original = StandardRgbColor(
                red = UnitInterval(0.3),
                green = UnitInterval(0.6),
                blue = UnitInterval(0.9),
            )

            val roundTripped = original.toLinear().toSrgb()

            assertRgbEquals(expected = original, actual = roundTripped)
        }

    }

    @Nested
    inner class ToHsb {

        @Test
        fun `black has zero brightness`() {
            val black = StandardRgbColor(UnitInterval.zero, UnitInterval.zero, UnitInterval.zero)

            val actual = black.toHsb()

            assertEquals(0.0, actual.brightness.value, 0.001)
        }

        @Test
        fun `white is achromatic at full brightness`() {
            val white = StandardRgbColor(UnitInterval.one, UnitInterval.one, UnitInterval.one)

            val actual = white.toHsb()

            assertEquals(0.0, actual.saturation.value, 0.001)
            assertEquals(1.0, actual.brightness.value, 0.001)
        }

        @Test
        fun `red has 0 degree hue`() {
            val red = StandardRgbColor(UnitInterval.one, UnitInterval.zero, UnitInterval.zero)

            val actual = red.toHsb()

            assertEquals(0.0, actual.hue.degrees, 0.001)
            assertEquals(1.0, actual.saturation.value, 0.001)
            assertEquals(1.0, actual.brightness.value, 0.001)
        }

        @Test
        fun `green has 120 degree hue`() {
            val green = StandardRgbColor(UnitInterval.zero, UnitInterval.one, UnitInterval.zero)

            val actual = green.toHsb()

            assertEquals(120.0, actual.hue.degrees, 0.001)
            assertEquals(1.0, actual.saturation.value, 0.001)
            assertEquals(1.0, actual.brightness.value, 0.001)
        }

        @Test
        fun `blue has 240 degree hue`() {
            val blue = StandardRgbColor(UnitInterval.zero, UnitInterval.zero, UnitInterval.one)

            val actual = blue.toHsb()

            assertEquals(240.0, actual.hue.degrees, 0.001)
            assertEquals(1.0, actual.saturation.value, 0.001)
            assertEquals(1.0, actual.brightness.value, 0.001)
        }

        @Test
        fun `converting and back yields the original color`() {
            val original = StandardRgbColor(
                red = UnitInterval(0.8),
                green = UnitInterval(0.3),
                blue = UnitInterval(0.5),
            )

            val roundTripped = original.toHsb().toRgb()

            assertRgbEquals(expected = original, actual = roundTripped)
        }
    }

}