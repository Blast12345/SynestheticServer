package math.physics

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LightTests {

    private val red = Light(1.0, 0.0, 0.0)
    private val green = Light(0.0, 1.0, 0.0)
    private val blue = Light(0.0, 0.0, 1.0)
    private val yellow = Light(1.0, 1.0, 0.0)
    private val cyan = Light(0.0, 1.0, 1.0)
    private val white = Light(1.0, 1.0, 1.0)
    private val black = Light(0.0, 0.0, 0.0)

    // Init
    @Test
    fun `the levels are zero by default`() {
        val light = Light()

        assertEquals(0.0, light.red)
        assertEquals(0.0, light.green)
        assertEquals(0.0, light.blue)
    }

    // Intensity
    @Test
    fun `get the overall intensity of the light`() {
        val sut = Light(1.0, 2.0, 3.0)

        assertEquals(6.0, sut.radiantFlux, 0.001)
    }

    // Summation
    @Test
    fun `combine two lights`() {
        val combined = red + green

        assertEquals(1.0, combined.red)
        assertEquals(1.0, combined.green)
        assertEquals(0.0, combined.blue)
    }

    // Multiplication
    @Test
    fun `scaling a light multiplies each channel`() {
        val light = Light(1.0, 2.0, 3.0)

        val actual = light.times(3.0)

        assertEquals(3.0, actual.red)
        assertEquals(6.0, actual.green)
        assertEquals(9.0, actual.blue)
    }

}