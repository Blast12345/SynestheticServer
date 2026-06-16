package lightOrgan.color

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextSpectralPeaks
import toolkit.monkeyTest.nextStandardRgbColor

@OptIn(ExperimentalCoroutinesApi::class)
class ColorManagerTests {

    private val colorAlgorithm: ColorAlgorithm = mockk()

    private val spectralPeaks = nextSpectralPeaks()
    private val color = nextStandardRgbColor()

    @BeforeEach
    fun setupHappyPath() {
        every { colorAlgorithm.calculate(spectralPeaks) } returns color
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(): ColorManager {
        return ColorManager(
            colorAlgorithm = colorAlgorithm,
        )
    }

    @Test
    fun `calculate a color from frequency bins`() {
        val sut = createSUT()

        val actual = sut.calculate(spectralPeaks)

        assertEquals(color, actual)
        assertEquals(color, sut.color.value)
    }

}