package lightOrgan.spectrum

import dsp.filtering.FilterConfig
import dsp.filtering.FilterFamily
import dsp.filtering.FilterOrder
import dsp.peakExtraction.nearestTo
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import toolkit.generators.TestToneGenerator
import toolkit.generators.Tone
import toolkit.minimalAppConfig

class SpectralAnalyzerIntegrationTests {

    private val minimalConfig = minimalAppConfig.spectralAnalysis
    private val frequencyTolerance = minimalConfig.spectrumCalculator.approximateBinSpacing
    private val magnitudeTolerance = 0.1f

    private val toneGenerator = TestToneGenerator.mono()
    private val tone1 = Tone(60f)
    private val tone2 = Tone(120f)

    private fun createSUT(): SpectralAnalyzer {
        return SpectralAnalyzer()
    }

    // Spectrum
    @Test
    fun `given a tone, the spectrum's loudest bin corresponds to the tone`() {
        val sut = createSUT()

        val frame = toneGenerator.generate(tone1)
        val spectrum = sut.analyze(frame, minimalConfig).spectrum

        val loudestBin = spectrum.maxBy { it.magnitude }
        assertEquals(tone1.frequency, loudestBin.frequency, frequencyTolerance)
        assertEquals(tone1.amplitude, loudestBin.magnitude, magnitudeTolerance)
    }

    // Peaks
    // I'd love to validate the exact number of peaks, but sidelobes are indistinguishable from peaks
    @Test
    fun `given silence, then there are no peaks`() {
        val sut = createSUT()

        val frame = toneGenerator.silence()
        val peaks = sut.analyze(frame, minimalConfig).peaks

        assertEquals(0, peaks.size)
    }

    @Test
    fun `given a tone, there is a peak corresponding to the tone`() {
        val sut = createSUT()

        val frame = toneGenerator.generate(tone1)
        val peaks = sut.analyze(frame, minimalConfig).peaks

        val strongestPeak = peaks.maxBy { it.magnitude }
        assertEquals(tone1.frequency, strongestPeak.frequency, frequencyTolerance)
        assertEquals(tone1.amplitude, strongestPeak.magnitude, magnitudeTolerance)
    }

    @Test
    fun `given multiple tones, there are multiple peaks corresponding to the tones`() {
        val sut = createSUT()

        val frame = toneGenerator.generate(tone1, tone2)
        val peaks = sut.analyze(frame, minimalConfig).peaks

        val peak1 = peaks.nearestTo(tone1.frequency)!!
        assertEquals(tone1.frequency, peak1.frequency, frequencyTolerance)
        assertEquals(tone1.amplitude, peak1.magnitude, magnitudeTolerance)

        val peak2 = peaks.nearestTo(tone2.frequency)!!
        assertEquals(tone2.frequency, peak2.frequency, frequencyTolerance)
        assertEquals(tone2.amplitude, peak2.magnitude, magnitudeTolerance)
    }

    // Passband
    private val highPassCutoff = 100f
    private val lowPassCutoff = 1000f
    private val filterDbPerOctave = 48
    private val rolloffThreshold = -filterDbPerOctave.toFloat() // Ensures that our passband is an octave from the cutoffs
    private val outOfBandTone = Tone(highPassCutoff / 4f) // Octave below passband
    private val inBandTone = Tone(highPassCutoff * 3f) // Octave into passband

    private val passbandConfig = minimalConfig.copy(
        audioConditioner = minimalConfig.audioConditioner.copy(
            rolloffThresholdDb = rolloffThreshold,
            highPassFilter = FilterConfig.HighPass(
                frequency = highPassCutoff,
                family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(filterDbPerOctave)),
            ),
            lowPassFilter = FilterConfig.LowPass(
                frequency = lowPassCutoff,
                family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(filterDbPerOctave)),
            ),
        )
    )

    @Test
    fun `the spectrum is confined to the passband`() {
        val sut = createSUT()

        val frame = toneGenerator.silence()
        val spectrum = sut.analyze(frame, passbandConfig).spectrum

        assertTrue(spectrum.isNotEmpty(), "Spectrum should not be empty.")
        assertTrue(spectrum.all { it.frequency in passbandConfig.audioConditioner.passband }, "No bins should escape the passband.")
    }

    @Test
    fun `the peaks are confined to the passband`() {
        val sut = createSUT()

        val frame = toneGenerator.generate(outOfBandTone, inBandTone)
        val peaks = sut.analyze(frame, passbandConfig).peaks

        val inBandPeak = peaks.nearestTo(inBandTone.frequency)!!
        assertEquals(inBandTone.frequency, inBandPeak.frequency, frequencyTolerance, "In-band tone should survive filtering.")
        assertTrue(peaks.all { it.frequency in passbandConfig.audioConditioner.passband }, "No peaks should escape the passband.")
    }

}