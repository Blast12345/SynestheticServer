package dsp.windowing

import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GeneralizedCosineWindowTests {

    // Using Hann because its simple
    private val hannCoefficients = floatArrayOf(0.5f, 0.5f)
    private val frame = FloatArray(5) { 1f }

    // Core calculations
    @Test
    fun `coefficients match expected shape`() {
        val sut = GeneralizedCosineWindow(hannCoefficients)

        val result = sut.coefficients(5)

        val expected = floatArrayOf(0.0f, 0.5f, 1.0f, 0.5f, 0.0f)
        assertArrayEquals(expected, result, 0.01f)
    }

    @Test
    fun `magnitude correction factor accounts for window size`() {
        val sut = GeneralizedCosineWindow(hannCoefficients)

        val smallWindow = sut.magnitudeCorrectionFactor(5)
        val largeWindow = sut.magnitudeCorrectionFactor(512)

        assertEquals(2.5f, smallWindow, 0.01f)
        assertEquals(2.004f, largeWindow, 0.01f)
    }

    @Test
    fun `energy correction factor accounts for window size`() {
        val sut = GeneralizedCosineWindow(hannCoefficients)

        val smallWindow = sut.energyCorrectionFactor(5)
        val largeWindow = sut.energyCorrectionFactor(512)

        assertEquals(1.826f, smallWindow, 0.01f)
        assertEquals(1.633f, largeWindow, 0.01f)
    }

    // Apply with no correction
    @Test
    fun `edge samples are zero`() {
        val sut = GeneralizedCosineWindow(hannCoefficients)

        val result = sut.appliedTo(frame)

        assertEquals(0f, result.first())
        assertEquals(0f, result.last())
    }

    @Test
    fun `center sample is unchanged`() {
        val sut = GeneralizedCosineWindow(hannCoefficients)

        val result = sut.appliedTo(frame)

        assertEquals(1f, result[2], 0.01f)
    }

    @Test
    fun `output is symmetric`() {
        val sut = GeneralizedCosineWindow(hannCoefficients)

        val result = sut.appliedTo(frame)

        assertEquals(result[0], result[4])
        assertEquals(result[1], result[3])
    }

    // Apply with magnitude correction
    @Test
    fun `apply window with magnitude correction`() {
        val sut = GeneralizedCosineWindow(hannCoefficients)

        val uncorrected = sut.appliedTo(frame)
        val corrected = sut.appliedTo(frame, Window.CorrectionType.MAGNITUDE)

        val expectedFactor = sut.magnitudeCorrectionFactor(frame.size)

        uncorrected.zip(corrected.toTypedArray()).forEach { (base, scaled) ->
            assertEquals(base * expectedFactor, scaled, 0.001f)
        }
    }

    // Apply with energy correction
    @Test
    fun `apply window with energy correction`() {
        val sut = GeneralizedCosineWindow(hannCoefficients)

        val uncorrected = sut.appliedTo(frame)
        val corrected = sut.appliedTo(frame, Window.CorrectionType.ENERGY)

        val expectedFactor = sut.energyCorrectionFactor(frame.size)

        uncorrected.zip(corrected.toTypedArray()).forEach { (base, scaled) ->
            assertEquals(base * expectedFactor, scaled, 0.001f)
        }
    }

}