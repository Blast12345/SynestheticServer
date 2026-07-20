package dsp.peakExtraction

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class SpectralPeakTests {

    private val peakAt100 = SpectralPeak(frequency = 100f, magnitude = 2f)
    private val peakAt200 = SpectralPeak(frequency = 200f, magnitude = 3f)
    private val peakAt300 = SpectralPeak(frequency = 300f, magnitude = 6f)

    private val peaks = listOf(peakAt100, peakAt200, peakAt300)

    // Nearest to frequency
    @Test
    fun `given a frequency, get the nearest peak`() {
        val actual = peaks.nearestTo(201f)

        assertEquals(peakAt200, actual)
    }

    @Test
    fun `given a frequency but no peaks, the nearest peak is null`() {
        val emptyList = emptyList<SpectralPeak>()

        val actual = emptyList.nearestTo(201f)

        assertNull(actual)
    }

    // Combined magnitude
    @Test
    fun `magnitudes of peaks are combined using root sum of squares`() {
        assertEquals(7.0, peaks.combinedMagnitude, 0.001)
    }

}