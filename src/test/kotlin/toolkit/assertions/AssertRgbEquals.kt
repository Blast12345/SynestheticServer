package toolkit.assertions

import color.RgbColor
import color.RgbColorSpace
import org.junit.jupiter.api.Assertions.assertEquals

fun <S : RgbColorSpace> assertRgbEquals(
    expected: RgbColor<S>,
    actual: RgbColor<S>,
    tolerance: Double = 1e-6,
) {
    assertEquals(expected.red.value, actual.red.value, tolerance, "red")
    assertEquals(expected.green.value, actual.green.value, tolerance, "green")
    assertEquals(expected.blue.value, actual.blue.value, tolerance, "blue")
}