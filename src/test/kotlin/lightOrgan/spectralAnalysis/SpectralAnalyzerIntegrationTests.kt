package lightOrgan.spectrum

import dsp.bins.nearestTo
import dsp.peakExtraction.nearestTo
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import lightOrgan.spectralAnalysis.noiseReduction.SpectralGate
import lightOrgan.spectralAnalysis.postProcessing.PostProcessorConfig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import toolkit.generators.TestToneGenerator
import toolkit.generators.Tone
import toolkit.minimalAppConfig

class SpectralAnalyzerIntegrationTests {

    private val minimalConfig = minimalAppConfig.spectralAnalysis
    private val frequencyTolerance = minimalConfig.spectrumCalculator.approximateBinSpacing
    private val magnitudeTolerance = 0.1f
    private val noiseFloor = -24f // safely above the sidelobes of most windows

    private val toneGenerator = TestToneGenerator.mono()
    private val tone1 = Tone(60f)
    private val tone2 = Tone(120f)
    private val backgroundTone = Tone.fromDbfs(200f, noiseFloor - 3f)

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

    // Noise Reduction
    private val noiseReductionConfig = minimalConfig.copy(
        postProcessor = PostProcessorConfig(
            noiseReducer = SpectralGate.Config(noiseFloor.toDouble())
        )
    )

    @Test
    fun `mitigate background noise in the spectrum`() {
        val sut = createSUT()
        val frame = toneGenerator.generate(tone1, backgroundTone)

        val spectrum = sut.analyze(frame, noiseReductionConfig).spectrum

        val bin1 = spectrum.nearestTo(tone1.frequency)!!
        assertEquals(tone1.frequency, bin1.frequency, frequencyTolerance)
        assertEquals(tone1.amplitude, bin1.magnitude, magnitudeTolerance)

        val bin2 = spectrum.nearestTo(backgroundTone.frequency)!!
        assertEquals(backgroundTone.frequency, bin2.frequency, frequencyTolerance)
        assertEquals(0f, bin2.magnitude, magnitudeTolerance)
    }

    @Test
    fun `mitigate background noise in the peaks`() {
        val sut = createSUT()
        val frame = toneGenerator.generate(tone1, backgroundTone)

        val peaks = sut.analyze(frame, noiseReductionConfig).peaks

        assertEquals(1, peaks.size)
        assertEquals(tone1.frequency, peaks.first().frequency, frequencyTolerance)
        assertEquals(tone1.amplitude, peaks.first().magnitude, magnitudeTolerance)
    }

}