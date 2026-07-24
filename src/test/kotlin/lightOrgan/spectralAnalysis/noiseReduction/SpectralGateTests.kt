package lightOrgan.spectralAnalysis.noiseReduction

import dsp.bins.FrequencyBin
import dsp.peakExtraction.SpectralPeak
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.math.pow

class SpectralGateTests {

    private val thresholdDb = -20.0
    private val threshold = 10.0.pow(thresholdDb / 20f)
    private val sut = SpectralGate(SpectralGate.Config(thresholdDb))

    // Spectrum
    @Test
    fun `bins below threshold are zeroed`() {
        val bin = FrequencyBin(frequency = 100f, magnitude = threshold / 2)

        val result = sut.reduceSpectrum(listOf(bin))

        assertEquals(0f, result.first().magnitude)
    }

    @Test
    fun `bins above threshold are attenuated`() {
        val bin = FrequencyBin(frequency = 100f, magnitude = 0.5)

        val result = sut.reduceSpectrum(listOf(bin))

        assertTrue(result.first().magnitude > 0f)
        assertTrue(result.first().magnitude < bin.magnitude)
    }

    // Peaks
    @Test
    fun `peaks below threshold are removed`() {
        val peak = SpectralPeak(frequency = 100f, magnitude = (threshold / 2).toFloat())

        val result = sut.reducePeaks(listOf(peak))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `peaks at threshold are removed`() {
        val peak = SpectralPeak(frequency = 100f, magnitude = (threshold).toFloat())

        val result = sut.reducePeaks(listOf(peak))

        assertTrue(result.isEmpty())
    }

    @Test
    fun `peaks above threshold are attenuated`() {
        val peak = SpectralPeak(frequency = 100f, magnitude = 0.5f)

        val result = sut.reducePeaks(listOf(peak))

        assertEquals(1, result.size)
        assertTrue(result.first().magnitude > 0f)
        assertTrue(result.first().magnitude < peak.magnitude)
    }
}