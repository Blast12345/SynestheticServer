package lightOrgan.spectrum

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import dsp.filtering.FilterConfig
import dsp.filtering.FilterFamily
import dsp.filtering.FilterOrder
import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.AudioConditionerConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import lightOrgan.spectralAnalysis.SpectrumCalculatorConfig
import lightOrgan.spectralAnalysis.noiseReduction.SpectralGate
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import toolkit.generators.combineWaves
import toolkit.generators.generateSilence
import toolkit.generators.generateSineWave
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

class SpectralAnalyzerIntegrationTests {

    private val minimalConfig = SpectralAnalysisConfig(
        audioConditioner = AudioConditionerConfig(
            gainDb = 0f,
            highPassFilter = null,
            lowPassFilter = null,
            rolloffThreshold = null,
            decimate = false
        ),
        spectrumCalculator = SpectrumCalculatorConfig(
            window = WindowType.Hann,
            frameDuration = 50.milliseconds, // 20 Hz spacing
            approximateBinSpacing = 1f,
        ),
        peakExtractor = PeakExtractorConfig.Parabolic,
        noiseReducer = null
    )

    private val audioFormat = AudioFormat(48000f, 16, 1)
    private val silence = generateSilence(audioFormat.sampleRate)
    private val wave1 = generateSineWave(60f, sampleRate = audioFormat.sampleRate)
    private val wave2 = generateSineWave(120f, sampleRate = audioFormat.sampleRate)
    private val combinedWaves = combineWaves(wave1.waveForm, wave2.waveForm)

    private val silenceFrame = AudioFrame(silence.samples, audioFormat)
    private val wave1Frame = AudioFrame(wave1.waveForm.samples, audioFormat)
    private val combinedWavesFrame = AudioFrame(combinedWaves.samples, audioFormat)

    private fun createSUT() = SpectralAnalyzer()

    // Spectrum
    @Test
    fun `given a tone, the spectrum's loudest bin corresponds to the tone`() {
        val sut = createSUT()

        val spectrum = sut.analyze(wave1Frame, minimalConfig).spectrum

        val peakBin = spectrum.maxBy { it.magnitude }
        assertEquals(wave1.frequency, peakBin.frequency, minimalConfig.spectrumCalculator.approximateBinSpacing)
        assertEquals(wave1.amplitude, peakBin.magnitude, 0.1f)
    }

    // Peaks
    // NOTE: It's tempting to assume 1 tone means 1 peak, but sidelobes are mis-identified as peaks
    @Test
    fun `given silence, then there are no peaks`() {
        val sut = createSUT()

        val peaks = sut.analyze(silenceFrame, minimalConfig).peaks

        assertEquals(0, peaks.size)
    }

    @Test
    fun `given a tone, there is a peak corresponding to the tone`() {
        val sut = createSUT()

        val peaks = sut.analyze(wave1Frame, minimalConfig).peaks

        val strongestPeak = peaks.maxBy { it.magnitude }
        assertEquals(wave1.frequency, strongestPeak.frequency, minimalConfig.spectrumCalculator.approximateBinSpacing)
        assertEquals(wave1.amplitude, strongestPeak.magnitude, 0.1f)
    }

    @Test
    fun `given multiple tones, there are multiple peaks corresponding to the tones`() {
        val sut = createSUT()

        val peaks = sut.analyze(combinedWavesFrame, minimalConfig).peaks

        val peak1 = peaks.minBy { abs(it.frequency - wave1.frequency) }
        assertEquals(wave1.frequency, peak1.frequency, minimalConfig.spectrumCalculator.approximateBinSpacing)
        assertEquals(wave1.amplitude, peak1.magnitude, 0.1f)

        val peak2 = peaks.minBy { abs(it.frequency - wave2.frequency) }
        assertEquals(wave2.frequency, peak2.frequency, minimalConfig.spectrumCalculator.approximateBinSpacing)
        assertEquals(wave2.amplitude, peak2.magnitude, 0.1f)
    }

    // Passband
    private val passbandConfig = minimalConfig.copy(
        audioConditioner = minimalConfig.audioConditioner.copy(
            rolloffThreshold = -48f,
            highPassFilter = FilterConfig.HighPass(
                frequency = 100f,
                family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
            ),
            lowPassFilter = FilterConfig.LowPass(
                frequency = 1000f,
                family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
            ),
        )
    )

    @Test
    fun `the spectrum is confined to the passband`() {
        val sut = createSUT()

        val spectrum = sut.analyze(silenceFrame, passbandConfig).spectrum

        assertTrue(spectrum.all { it.frequency in passbandConfig.audioConditioner.passband })
    }

    @Test
    fun `the peaks are confined to the passband`() {
        val sut = createSUT()
        val outOfBandTone = toneFrame(30f, 0.0)

        val peaks = sut.analyze(outOfBandTone, passbandConfig).peaks

        assertTrue(peaks.all { it.frequency in passbandConfig.audioConditioner.passband })
    }

    // Noise Reduction
    private val gateAt24Db = SpectralGate.Config(thresholdDb = -24.0)

    private fun toneFrame(frequency: Float, amplitudeDb: Double): AudioFrame {
        val tone = generateSineWave(frequency, amplitudeDb, audioFormat.sampleRate)
        return AudioFrame(tone.waveForm.samples, audioFormat)
    }

    @Test
    fun `a tone below the noise gate is removed from the spectrum`() {
        val sut = createSUT()
        val quietTone = toneFrame(60f, amplitudeDb = -30.0)

        val spectrum = sut.analyze(quietTone, minimalConfig.copy(noiseReducer = gateAt24Db)).spectrum

        assertTrue(spectrum.all { it.magnitude == 0f })
    }

    @Test
    fun `a tone above the noise gate stays in the spectrum`() {
        val sut = createSUT()
        val toneFrequency = 60f
        val loudTone = toneFrame(toneFrequency, amplitudeDb = 0.0)

        val spectrum = sut.analyze(loudTone, minimalConfig.copy(noiseReducer = gateAt24Db)).spectrum

        val strongestBin = spectrum.maxBy { it.magnitude } // TODO: Func to find bin closest to freq (with wiggle room)
        assertEquals(toneFrequency, strongestBin.frequency, minimalConfig.spectrumCalculator.approximateBinSpacing)
        assertTrue(strongestBin.magnitude > 0f)
    }

    @Test
    fun `a tone below the noise gate has no peak`() {
        val sut = createSUT()
        val quietTone = toneFrame(60f, amplitudeDb = -30.0)

        val peaks = sut.analyze(quietTone, minimalConfig.copy(noiseReducer = gateAt24Db)).peaks

        assertEquals(0, peaks.size)
    }

    @Test
    fun `a tone above the noise gate stays in the peaks`() {
        val sut = createSUT()
        val toneFrequency = 60f
        val loudTone = toneFrame(toneFrequency, amplitudeDb = 0.0)

        val peaks = sut.analyze(loudTone, minimalConfig.copy(noiseReducer = gateAt24Db)).peaks

        val strongestPeak = peaks.maxBy { it.magnitude }
        assertEquals(toneFrequency, strongestPeak.frequency, minimalConfig.spectrumCalculator.approximateBinSpacing)
        assertTrue(strongestPeak.magnitude > 0f)
    }

}