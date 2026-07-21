package lightOrgan.spectralAnalysis.spectrum

import audio.samples.AudioFormat
import dsp.windowing.WindowType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import toolkit.generators.combineWaves
import toolkit.generators.generateSilence
import toolkit.generators.generateSineWave
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

class SpectrumCalculatorIntegrationTests {

    private val resolution = 20f
    private val minimumDuration = (1000 / resolution).toLong().milliseconds

    private val config = SpectrumCalculatorConfig(
        frameDuration = minimumDuration * 2,
        approximateBinSpacing = 1f,
        window = WindowType.Hann
    )

    private val audioFormat = AudioFormat(10000f, 16, 1)
    private val silence = generateSilence(audioFormat.sampleRate)
    private val tone1 = generateSineWave(frequency = resolution * 10, sampleRate = audioFormat.sampleRate, duration = config.frameDuration)
    private val tone2 = generateSineWave(frequency = resolution * 100, sampleRate = audioFormat.sampleRate, duration = config.frameDuration)
    private val combinedTones = combineWaves(tone1.waveForm, tone2.waveForm)

    private fun createSUT(): SpectrumCalculator {
        return SpectrumCalculator()
    }

    // Magnitudes
    @Test
    fun `given silence, the magnitudes are zero`() {
        val sut = createSUT()

        val spectrum = sut.calculate(silence.toAudioFrame(), config)

        spectrum.forEach { assertEquals(0f, it.magnitude) }
    }

    @Test
    fun `given a tone, the loudest bin corresponds to the tone`() {
        val sut = createSUT()

        val spectrum = sut.calculate(tone1.waveForm.toAudioFrame(), config)

        val peakBin = spectrum.maxBy { it.magnitude }
        assertEquals(tone1.frequency, peakBin.frequency, config.approximateBinSpacing)
        assertEquals(tone1.amplitude, peakBin.magnitude, 0.1f)
    }

    @Test
    fun `given multiple tones, the loudest bins corresponds to the tones`() {
        val sut = createSUT()

        val spectrum = sut.calculate(combinedTones.toAudioFrame(), config)

        val peak1 = spectrum.minBy { abs(it.frequency - tone1.frequency) }
        assertEquals(tone1.frequency, peak1.frequency, config.approximateBinSpacing)
        assertEquals(tone1.amplitude, peak1.magnitude, 0.1f)

        val peak2 = spectrum.minBy { abs(it.frequency - tone2.frequency) }
        assertEquals(tone2.frequency, peak2.frequency, config.approximateBinSpacing)
        assertEquals(tone2.amplitude, peak2.magnitude, 0.1f)
    }

    // Rolling Buffer - always use the latest data
    @Test
    fun `given audio shorter than the frame duration, the spectrum reflects both new and retained audio`() {
        val sut = createSUT()
        sut.calculate(tone1.waveForm.toAudioFrame(), config)

        // Half duration means 50/50 split between old and new data
        val halfDurationTone2 = generateSineWave(tone2.frequency, sampleRate = audioFormat.sampleRate, duration = config.frameDuration / 2)
        val spectrum = sut.calculate(halfDurationTone2.waveForm.toAudioFrame(), config)

        val peak1 = spectrum.minBy { abs(it.frequency - tone1.frequency) }
        assertEquals(tone1.frequency, peak1.frequency, config.approximateBinSpacing)
        assertEquals(tone1.amplitude / 2f, peak1.magnitude, 0.1f)

        val peak2 = spectrum.minBy { abs(it.frequency - tone2.frequency) }
        assertEquals(tone2.frequency, peak2.frequency, config.approximateBinSpacing)
        assertEquals(tone2.amplitude / 2f, peak2.magnitude, 0.1f)
    }

    // Interpolation - helps reveal frequencies between bins
    @Test
    fun `interpolate to an approximate resolution`() {
        require(config.approximateBinSpacing < resolution) { "We cannot verify interpolation is occurring if the resolution not greater than the approximate spacing." }
        val sut = createSUT()

        val spectrum = sut.calculate(tone1.waveForm.toAudioFrame(), config)

        val binWidth = spectrum[1].frequency - spectrum[0].frequency
        assertTrue(binWidth <= config.approximateBinSpacing)
    }

    // Invalid bins
    @Test
    fun `the DC offset bin is omitted`() {
        val sut = createSUT()

        val spectrum = sut.calculate(tone1.waveForm.toAudioFrame(), config)

        // DC offset doesn't represent a real frequency
        assertTrue(spectrum.none { it.frequency == 0f })
    }

    @Test
    fun `the Nyquist bin is omitted`() {
        val sut = createSUT()

        val spectrum = sut.calculate(tone1.waveForm.toAudioFrame(), config)

        // Nyquist lacks the imaginary component, so calculating the magnitude is unreliable
        assertTrue(spectrum.none { it.frequency == audioFormat.nyquistFrequency })
    }

}