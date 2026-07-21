package dsp.bins

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class FrequencyBinTests {

    private val binAt100 = FrequencyBin(frequency = 100f, magnitude = 2.0, phase = 0.0)
    private val binAt200 = FrequencyBin(frequency = 200f, magnitude = 3.0, phase = 0.0)
    private val binAt300 = FrequencyBin(frequency = 300f, magnitude = 6.0, phase = 0.0)

    private val bins = listOf(binAt100, binAt200, binAt300)

    // Nearest to frequency
    @Test
    fun `given a frequency, get the nearest bin`() {
        val actual = bins.nearestTo(201f)

        assertEquals(binAt200, actual)
    }

    @Test
    fun `given a frequency but no bins, the nearest bin is null`() {
        val emptyList = emptyList<FrequencyBin>()

        val actual = emptyList.nearestTo(201f)

        assertNull(actual)
    }

}