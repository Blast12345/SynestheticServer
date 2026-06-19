package lightOrgan.spectrum

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import toolkit.generators.combineWaves
import toolkit.generators.generateSilence
import toolkit.generators.generateSineWave
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds

class SpectralAnalyzerIntegrationTests {

    private val config = SpectralAnalysisConfig(
        gainDb = 0f,
        frameDuration = 50.milliseconds, // 20 Hz spacing
        approximateBinSpacing = 1f,
        rolloffThreshold = -48f,
        highPassFilter = null,
        lowPassFilter = null,
        window = WindowType.Hann
    )

    private val audioFormat = AudioFormat(48000f, 16, 1)
    private val silence = generateSilence(audioFormat.sampleRate)
    private val wave1 = generateSineWave(60f, sampleRate = audioFormat.sampleRate)
    private val wave2 = generateSineWave(120f, sampleRate = audioFormat.sampleRate)
    private val combinedWaves = combineWaves(wave1.waveForm, wave2.waveForm)

    private val silenceFrame = AudioFrame(silence.samples, audioFormat)
    private val wave1Frame = AudioFrame(wave1.waveForm.samples, audioFormat)
    private val combinedWavesFrame = AudioFrame(combinedWaves.samples, audioFormat)

    private fun createSUT(config: SpectralAnalysisConfig = this.config): SpectralAnalyzer {
        return SpectralAnalyzer(config)
    }

    // Spectrum
    @Test
    fun `given a tone, the spectrum's loudest bin corresponds to the tone`() {
        val sut = createSUT()

        val spectrum = sut.analyze(wave1Frame).spectrum

        val peakBin = spectrum.maxBy { it.magnitude }
        assertEquals(wave1.frequency, peakBin.frequency, config.approximateBinSpacing)
        assertEquals(wave1.amplitude, peakBin.magnitude, 0.1f)
    }

    // Peaks
    // I'd love to validate the exact number of peaks, but in practice this is very hard to make happen.
    @Test
    fun `given silence, then there are no peaks`() {
        val sut = createSUT()

        val peaks = sut.analyze(silenceFrame).peaks

        assertEquals(0, peaks.size)
    }

    @Test
    fun `given a tone, there is a peak corresponding to the tone`() {
        val sut = createSUT()

        val peaks = sut.analyze(wave1Frame).peaks

        val strongestPeak = peaks.maxBy { it.magnitude }
        assertEquals(wave1.frequency, strongestPeak.frequency, config.approximateBinSpacing)
        assertEquals(wave1.amplitude, strongestPeak.magnitude, 0.1f)
    }

    @Test
    fun `given multiple tones, there are multiple peaks corresponding to the tones`() {
        val sut = createSUT()

        val peaks = sut.analyze(combinedWavesFrame).peaks

        val peak1 = peaks.minBy { abs(it.frequency - wave1.frequency) }
        assertEquals(wave1.frequency, peak1.frequency, config.approximateBinSpacing)
        assertEquals(wave1.amplitude, peak1.magnitude, 0.1f)

        val peak2 = peaks.minBy { abs(it.frequency - wave2.frequency) }
        assertEquals(wave2.frequency, peak2.frequency, config.approximateBinSpacing)
        assertEquals(wave2.amplitude, peak2.magnitude, 0.1f)
    }

}