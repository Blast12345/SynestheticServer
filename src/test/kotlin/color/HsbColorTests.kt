package color

import math.geometry.Angle
import math.normalization.UnitInterval
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import toolkit.assertions.assertRgbEquals

class HsbColorTests {

    @Nested
    inner class ChromaticityAccessor {

        @Test
        fun `zero saturation is achromatic`() {
            val white = HsbColor<StandardRGB>(Angle.fromDegrees(0.0), UnitInterval.zero, UnitInterval.one)

            val actual = white.chromaticity

            assertEquals(Chromaticity.Achromatic, actual)
        }

        @Test
        fun `nonzero saturation is chromatic with matching hue and saturation`() {
            val red = HsbColor<StandardRGB>(Angle.fromDegrees(0.0), UnitInterval.one, UnitInterval.one)

            val actual = red.chromaticity as Chromaticity.Chromatic

            assertEquals(0.0, actual.hue.degrees, 0.001)
            assertEquals(1.0, actual.saturation.value, 0.001)
        }

        @Test
        fun `brightness does not affect chromaticity`() {
            val bright = HsbColor<StandardRGB>(Angle.fromDegrees(120.0), UnitInterval(0.8), UnitInterval.one)
            val dark = HsbColor<StandardRGB>(Angle.fromDegrees(120.0), UnitInterval(0.8), UnitInterval(0.2))

            assertEquals(bright.chromaticity, dark.chromaticity)
        }

    }

    @Nested
    inner class ToRgb {

        @Test
        fun `black`() {
            val hsb = HsbColor<StandardRGB>(Angle.zero, UnitInterval.zero, UnitInterval.zero)

            val actual = hsb.toRgb()

            assertRgbEquals(expected = StandardRgbColor(UnitInterval.zero, UnitInterval.zero, UnitInterval.zero), actual = actual)
        }

        @Test
        fun `white`() {
            val hsb = HsbColor<StandardRGB>(Angle.zero, UnitInterval.zero, UnitInterval.one)

            val actual = hsb.toRgb()

            assertRgbEquals(expected = StandardRgbColor(UnitInterval.one, UnitInterval.one, UnitInterval.one), actual = actual)
        }

        @Test
        fun `red`() {
            val hsb = HsbColor<StandardRGB>(Angle.fromDegrees(0.0), UnitInterval.one, UnitInterval.one)

            val actual = hsb.toRgb()

            assertRgbEquals(expected = StandardRgbColor(UnitInterval.one, UnitInterval.zero, UnitInterval.zero), actual = actual)
        }

        @Test
        fun `green`() {
            val hsb = HsbColor<StandardRGB>(Angle.fromDegrees(120.0), UnitInterval.one, UnitInterval.one)

            val actual = hsb.toRgb()

            assertRgbEquals(expected = StandardRgbColor(UnitInterval.zero, UnitInterval.one, UnitInterval.zero), actual = actual)
        }

        @Test
        fun `blue`() {
            val hsb = HsbColor<StandardRGB>(Angle.fromDegrees(240.0), UnitInterval.one, UnitInterval.one)

            val actual = hsb.toRgb()

            assertRgbEquals(expected = StandardRgbColor(UnitInterval.zero, UnitInterval.zero, UnitInterval.one), actual = actual)
        }

        @Test
        fun `reduced saturation desaturates toward white`() {
            val hsb = HsbColor<StandardRGB>(Angle.fromDegrees(0.0), UnitInterval(0.5), UnitInterval.one)

            val actual = hsb.toRgb()

            assertRgbEquals(
                expected = StandardRgbColor(UnitInterval.one, UnitInterval(0.5), UnitInterval(0.5)),
                actual = actual
            )
        }

        @Test
        fun `reduced brightness darkens`() {
            val hsb = HsbColor<StandardRGB>(Angle.fromDegrees(0.0), UnitInterval.one, UnitInterval(0.5))

            val actual = hsb.toRgb()

            assertRgbEquals(
                expected = StandardRgbColor(UnitInterval(0.5), UnitInterval.zero, UnitInterval.zero),
                actual = actual
            )
        }

        @Test
        fun `converting and back yields the original color`() {
            val original = HsbColor<StandardRGB>(
                hue = Angle.fromDegrees(210.0),
                saturation = UnitInterval(0.7),
                brightness = UnitInterval(0.8)
            )

            val roundTripped = original.toRgb().toHsb()

            assertEquals(original.hue.degrees, roundTripped.hue.degrees, 0.001)
            assertEquals(original.saturation.value, roundTripped.saturation.value, 0.001)
            assertEquals(original.brightness.value, roundTripped.brightness.value, 0.001)
        }

    }

}