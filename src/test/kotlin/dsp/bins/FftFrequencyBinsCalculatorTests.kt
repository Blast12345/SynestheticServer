package dsp.bins

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import toolkit.generators.generateSilence
import toolkit.generators.generateSineWave
import kotlin.time.Duration.Companion.seconds

class FftFrequencyBinsCalculatorTests {

    private val sampleRate = 1024f // Any rate that supports the frequency would be valid
    private val duration = 1.seconds // this gives us a 1 Hz resolution for easy testing

    private val tone = generateSineWave(10f, sampleRate = sampleRate, duration = duration)
    private val silence = generateSilence(sampleRate, duration)

    private fun createSUT() = FftFrequencyBinsCalculator()

    @Test
    fun `given silence, all bins have zero magnitudes`() {
        val sut = createSUT()

        val bins = sut.calculate(silence.samples, sampleRate)

        bins.forEach { assertEquals(0f, it.magnitude, 0.001f) }
    }

    @Test
    fun `given a tone, the peak bin corresponds to the tone`() {
        val sut = createSUT()

        val bins = sut.calculate(tone.waveForm.samples, sampleRate)

        val peakBin = bins.maxBy { it.magnitude }
        assertEquals(tone.frequency, peakBin.frequency, 0.001f)
        assertEquals(tone.amplitude, peakBin.magnitude, 0.001f)
    }

    @Test
    fun `bin frequencies are spaced by the frequency resolution`() {
        val sut = createSUT()

        val bins = sut.calculate(tone.waveForm.samples, sampleRate)

        val expectedSpacing = sampleRate / tone.waveForm.samples.size
        bins.zipWithNext().forEach { (current, next) ->
            assertEquals(expectedSpacing, next.frequency - current.frequency, 0.01f)
        }
    }

    @Test
    fun `the DC offset not doubled`() {
        val sut = createSUT()
        val dcOffset = 3f
        val samplesAtDC = FloatArray(sampleRate.toInt()) { dcOffset }

        val bins = sut.calculate(samplesAtDC, sampleRate)

        val dcBin = bins[0]
        assertEquals(0f, dcBin.frequency, 0.001f)
        assertEquals(dcOffset, dcBin.magnitude, 0.001f)
    }

    @Test
    fun `Nyquist bin is not doubled`() {
        val sut = createSUT()
        val nyquistFrequency = sampleRate / 2f
        val amplitude = 1f

        // Apparently generating a tone for the Nyquist frequency requires a cosine wave?
        val nyquistCosine = FloatArray(sampleRate.toInt()) { if (it % 2 == 0) amplitude else -amplitude }
        val bins = sut.calculate(nyquistCosine, sampleRate)

        val nyquistBin = bins.last()
        assertEquals(nyquistFrequency, nyquistBin.frequency, 0.001f)
        assertEquals(amplitude, nyquistBin.magnitude, 0.001f)
    }

}