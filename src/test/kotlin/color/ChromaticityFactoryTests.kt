package color

import color.rgb.Chromaticity
import color.rgb.ChromaticityFactory
import math.physics.Light
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class ChromaticityFactoryTest {

    private fun createSUT(): ChromaticityFactory = ChromaticityFactory()

    @Test
    fun `no light returns null`() {
        val sut = createSUT()
        val noLight = Light(0.0, 0.0, 0.0)

        val actual = sut.fromLight(noLight)

        assertNull(actual)
    }

    @Test
    fun `white light is achromatic`() {
        val sut = createSUT()
        val white = Light(1.0, 1.0, 1.0)

        val actual = sut.fromLight(white)

        assertEquals(Chromaticity.Achromatic, actual)
    }

    @Test
    fun `red light has 0 degree hue at full saturation`() {
        val sut = createSUT()
        val red = Light(1.0, 0.0, 0.0)

        val actual = sut.fromLight(red) as Chromaticity.Chromatic

        assertEquals(0.0, actual.hue.degrees, 0.01)
        assertEquals(1.0, actual.saturation.value, 0.01)
    }

    @Test
    fun `green light has 120 degree hue at full saturation`() {
        val sut = createSUT()
        val green = Light(0.0, 1.0, 0.0)

        val actual = sut.fromLight(green) as Chromaticity.Chromatic

        assertEquals(120.0, actual.hue.degrees, 0.01)
        assertEquals(1.0, actual.saturation.value, 0.01)
    }

    @Test
    fun `blue light has 240 degree hue at full saturation`() {
        val sut = createSUT()
        val blue = Light(0.0, 0.0, 1.0)

        val actual = sut.fromLight(blue) as Chromaticity.Chromatic

        assertEquals(240.0, actual.hue.degrees, 0.01)
        assertEquals(1.0, actual.saturation.value, 0.01)
    }

    @Test
    fun `chromaticity is independent of intensity`() {
        val sut = createSUT()
        val dim = Light(1.0, 0.0, 0.0)
        val bright = Light(5.0, 0.0, 0.0)

        val dimChromaticity = sut.fromLight(dim)
        val brightChromaticity = sut.fromLight(bright)

        assertEquals(dimChromaticity, brightChromaticity)
    }

    @Test
    fun `complementary lights produce achromatic`() {
        val sut = createSUT()
        val red = Light(1.0, 0.0, 0.0)
        val cyan = Light(0.0, 1.0, 1.0)

        val actual = sut.fromLight(red + cyan)

        assertEquals(Chromaticity.Achromatic, actual)
    }

}