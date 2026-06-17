package color

import math.normalization.UnitInterval
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import toolkit.assertions.assertRgbEquals

class LinearRgbColorTests {

    @Nested
    inner class ToSrgb {

        @Test
        fun `low value uses linear segment`() {
            val linear = LinearRgbColor(
                red = UnitInterval(0.002),
                green = UnitInterval(0.002),
                blue = UnitInterval(0.002),
            )

            val srgb = linear.toSrgb()

            assertRgbEquals(
                expected = StandardRgbColor(
                    red = UnitInterval(0.025840),
                    green = UnitInterval(0.025840),
                    blue = UnitInterval(0.025840),
                ),
                actual = srgb,
            )
        }

        @Test
        fun `mid value uses gamma curve`() {
            val linear = LinearRgbColor(
                red = UnitInterval(0.25),
                green = UnitInterval(0.25),
                blue = UnitInterval(0.25),
            )

            val srgb = linear.toSrgb()

            assertRgbEquals(
                expected = StandardRgbColor(
                    red = UnitInterval(0.537098729),
                    green = UnitInterval(0.537098729),
                    blue = UnitInterval(0.537098729),
                ),
                actual = srgb,
            )
        }

        @Test
        fun `converting and back yields the original color`() {
            val original = LinearRgbColor(
                red = UnitInterval(0.1),
                green = UnitInterval(0.4),
                blue = UnitInterval(0.7),
            )

            val roundTripped = original.toSrgb().toLinear()

            assertRgbEquals(expected = original, actual = roundTripped)
        }


    }


}