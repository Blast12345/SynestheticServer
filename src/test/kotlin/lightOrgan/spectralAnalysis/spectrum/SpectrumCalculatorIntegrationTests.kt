package lightOrgan.spectralAnalysis.spectrum

import dsp.bins.nearestTo
import dsp.windowing.WindowType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import toolkit.generators.TestToneGenerator
import toolkit.generators.Tone
import kotlin.time.Duration.Companion.seconds

class SpectrumCalculatorIntegrationTests {

    private val config = SpectrumCalculatorConfig(
        frameDuration = 1.seconds / 10, // 10 Hz resolution
        approximateBinSpacing = 1f, // 1 Hz resolution
        window = WindowType.Hann
    )

    private val toneGenerator = TestToneGenerator.mono(defaultDuration = config.frameDuration)
    private val tone1 = Tone(config.frequencyResolution * 10)
    private val tone2 = Tone(config.frequencyResolution * 100)

    private val frequencyTolerance = config.frequencyResolution

    private fun createSUT(): SpectrumCalculator {
        return SpectrumCalculator()
    }

    // Magnitudes
    @Test
    fun `given silence, the magnitudes are zero`() {
        val sut = createSUT()

        val frame = toneGenerator.silence()
        val spectrum = sut.calculate(frame, config)

        spectrum.forEach { assertEquals(0f, it.magnitude) }
    }

    @Test
    fun `given a tone, the loudest bin corresponds to the tone`() {
        val sut = createSUT()

        val frame = toneGenerator.generate(tone1)
        val spectrum = sut.calculate(frame, config)

        val peakBin = spectrum.maxBy { it.magnitude }
        assertEquals(tone1.frequency, peakBin.frequency, frequencyTolerance)
        assertEquals(tone1.amplitude, peakBin.magnitude, 0.1f)
    }

    @Test
    fun `given multiple tones, the loudest bins correspond to the tones`() {
        val sut = createSUT()

        val frame = toneGenerator.generate(tone1, tone2)
        val spectrum = sut.calculate(frame, config)

        val peak1 = spectrum.nearestTo(tone1.frequency)!!
        assertEquals(tone1.frequency, peak1.frequency, frequencyTolerance)
        assertEquals(tone1.amplitude, peak1.magnitude, 0.1f)

        val peak2 = spectrum.nearestTo(tone2.frequency)!!
        assertEquals(tone2.frequency, peak2.frequency, frequencyTolerance)
        assertEquals(tone2.amplitude, peak2.magnitude, 0.1f)
    }

    // Rolling Buffer - always use the latest data
    @Test
    fun `given audio shorter than the frame duration, the spectrum reflects both new and retained audio`() {
        val sut = createSUT()
        val fullFrame = toneGenerator.generate(tone1)
        val halfFrame = toneGenerator.generate(tone2, duration = config.frameDuration / 2)

        // Half duration means 50/50 split between old and new data
        val spectrum1 = sut.calculate(fullFrame, config)
        val spectrum2 = sut.calculate(halfFrame, config)

        val peak1 = spectrum2.nearestTo(tone1.frequency)!!
        assertEquals(tone1.frequency, peak1.frequency, frequencyTolerance)
        assertEquals(tone1.amplitude / 2f, peak1.magnitude, 0.1f)

        val peak2 = spectrum2.nearestTo(tone2.frequency)!!
        assertEquals(tone2.frequency, peak2.frequency, frequencyTolerance)
        assertEquals(tone2.amplitude / 2f, peak2.magnitude, 0.1f)
    }

    // Interpolation - helps reveal frequencies between bins
    @Test
    fun `interpolate to an approximate resolution`() {
        require(config.approximateBinSpacing < config.frequencyResolution) { "We cannot verify interpolation is occurring if the resolution not greater than the approximate spacing." }
        val sut = createSUT()

        val frame = toneGenerator.silence()
        val spectrum = sut.calculate(frame, config)

        val binWidth = spectrum[1].frequency - spectrum[0].frequency
        assertTrue(binWidth <= config.approximateBinSpacing)
    }

    // Invalid bins
    @Test
    fun `the DC offset bin is omitted`() {
        val sut = createSUT()

        val frame = toneGenerator.silence()
        val spectrum = sut.calculate(frame, config)

        // DC offset doesn't represent a real frequency
        assertTrue(spectrum.none { it.frequency == 0f })
    }


    @Test
    fun `bins below the frequency resolution are omitted`() {
        val sut = createSUT()

        val frame = toneGenerator.silence()
        val spectrum = sut.calculate(frame, config)

        // Bins below the resolution are not reliable
        assertTrue(spectrum.none { it.frequency < config.frequencyResolution })
    }

    @Test
    fun `the Nyquist bin is omitted`() {
        val sut = createSUT()

        val frame = toneGenerator.silence()
        val spectrum = sut.calculate(frame, config)

        // Nyquist lacks the imaginary component, so calculating the magnitude is unreliable
        assertTrue(spectrum.none { it.frequency == frame.format.nyquistFrequency })
    }

}