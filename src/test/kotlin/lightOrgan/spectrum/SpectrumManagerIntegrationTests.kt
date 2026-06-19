package lightOrgan.spectrum

import dsp.Decimator
import dsp.bins.nearestTo
import dsp.filtering.FilterConfig
import dsp.filtering.FilterFamily
import dsp.filtering.FilterOrder
import dsp.filtering.FilterType
import dsp.windowing.WindowType
import extensions.inSeconds
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import toolkit.generators.combineWaves
import toolkit.generators.generateSilence
import toolkit.generators.generateSineWave
import toolkit.monkeyTest.nextAudioFrame
import kotlin.time.Duration.Companion.milliseconds

// The processing chain is so long and specific that unit tests seemed like a mirror of implementation
// rather than checks for meaningful behavior. As such, integration tests seemed like the right tool.
class SpectrumManagerIntegrationTests {

    private val config = SpectrumConfig(
        gainDb = 0f,
        frameDuration = 50.milliseconds, // 20 Hz spacing
        approximateBinSpacing = 1f,
        rolloffThreshold = -48f,
        highPassFilter = null,
        lowPassFilter = null,
        window = WindowType.Hann
    )

    private val sampleRate = 48000f
    private val frequency1 = 60f
    private val frequency2 = 120f
    private val middleFrequency = (frequency1 + frequency2) / 2f
    private val wave1 = generateSineWave(frequency1, sampleRate = sampleRate)
    private val wave2 = generateSineWave(frequency2, sampleRate = sampleRate)
    private val combinedWaves = combineWaves(wave1.waveForm, wave2.waveForm)
    private val silence = generateSilence(sampleRate)

    private val wave1Frame = nextAudioFrame(wave1.waveForm)
    private val combinedWavesFrame = nextAudioFrame(combinedWaves)
    private val silenceFrame = nextAudioFrame(silence)

    val highPassConfig = FilterConfig(
        type = FilterType.HighPass(middleFrequency),
        family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
    )

    val lowPassConfig = FilterConfig(
        type = FilterType.LowPass(middleFrequency),
        family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
    )

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    // Frequency Bins
    @Test
    fun `given a tone, the peak bin is at the frequency of the wave`() {
        val sut = SpectrumManager(config)

        val bins = sut.calculate(wave1Frame)

        val peakBin = bins.maxBy { it.magnitude }
        assertEquals(frequency1, peakBin.frequency, config.approximateBinSpacing)
        assertEquals(1f, peakBin.magnitude, 0.1f)
    }

    @Test
    fun `silence produces near-zero magnitudes`() {
        val sut = SpectrumManager(config)

        val bins = sut.calculate(silenceFrame)

        bins.forEach { assertTrue(it.magnitude < 0.1f) }
    }

    @Test
    fun `given multiple tones, the peak bins are at the respective frequency of the waves`() {
        val sut = SpectrumManager(config)

        val bins = sut.calculate(combinedWavesFrame)

        val peak1 = bins.nearestTo(frequency1)
        assertEquals(frequency1, peak1.frequency, config.approximateBinSpacing)
        assertEquals(1f, peak1.magnitude, 0.1f)

        val peak2 = bins.nearestTo(frequency2)
        assertEquals(frequency2, peak2.frequency, config.approximateBinSpacing)
        assertEquals(1f, peak2.magnitude, 0.1f)
    }

    @Test
    fun `bin spacing approximately matches configured spacing`() {
        val sut = SpectrumManager(config)

        val bins = sut.calculate(wave1Frame)

        val maxSpacing = bins.zipWithNext { a, b -> b.frequency - a.frequency }.max()

        assertTrue(
            maxSpacing <= config.approximateBinSpacing,
            "Max bin spacing $maxSpacing exceeds configured spacing ${config.approximateBinSpacing}"
        )
    }

    @Test
    fun `bins below the frequency resolution are not included`() {
        val sut = SpectrumManager(
            config.copy(highPassFilter = null) // High pass may lead to false positives
        )

        val bins = sut.calculate(wave1Frame)

        val frequencyResolution = 1 / config.frameDuration.inSeconds
        val lowestBin = bins.minBy { it.frequency }
        assertTrue(lowestBin.frequency >= frequencyResolution)
    }

    // Multichannel
    @Test
    fun `stereo input produces correct results`() {
        val sut = SpectrumManager(config)
        val stereoFrame = nextAudioFrame(wave1.waveForm, silence)

        val bins = sut.calculate(stereoFrame)

        val peakBin = bins.maxBy { it.magnitude }
        assertEquals(frequency1, peakBin.frequency, config.approximateBinSpacing)
        assertEquals(0.5f, peakBin.magnitude, 0.1f) // Half-amplitude because only one channel (i.e., half) has the tone
    }

    // Gain
    @Test
    fun `given a 6 dB gain increase, the peak's magnitude is doubled`() {
        val sut = SpectrumManager(
            config.copy(gainDb = 6f)
        )

        val bins = sut.calculate(wave1Frame)

        val peakBin = bins.maxBy { it.magnitude }
        assertEquals(frequency1, peakBin.frequency, config.approximateBinSpacing)
        assertEquals(2f, peakBin.magnitude, 0.1f)
    }

    @Test
    fun `given a 6 dB gain reduction, the peak's magnitude is halved`() {
        val sut = SpectrumManager(
            config.copy(gainDb = -6f)
        )

        val bins = sut.calculate(wave1Frame)

        val peakBin = bins.maxBy { it.magnitude }
        assertEquals(frequency1, peakBin.frequency, config.approximateBinSpacing)
        assertEquals(0.5f, peakBin.magnitude, 0.1f)
    }


    // DSP Filters
    @Test
    fun `high pass filter attenuates frequencies below cutoff`() {
        val sut = SpectrumManager(config.copy(highPassFilter = highPassConfig))

        val bins = sut.calculate(combinedWavesFrame)

        val peak1 = bins.nearestTo(frequency1)
        val peak2 = bins.nearestTo(frequency2)

        assertEquals(0.1f, peak1.magnitude, 0.1f, "Expected $frequency1 Hz to be attenuated")
        assertEquals(1f, peak2.magnitude, 0.1f)
    }

    @Test
    fun `bins below high pass threshold are not included`() {
        val sut = SpectrumManager(config.copy(highPassFilter = highPassConfig))

        val bins = sut.calculate(wave1Frame)

        val threshold = highPassConfig.frequencyAt(config.rolloffThreshold)
        val lowestBin = bins.minBy { it.frequency }
        assertTrue(
            lowestBin.frequency >= threshold,
            "Expected no bins below high pass threshold of $threshold Hz"
        )
    }

    @Test
    fun `low pass filter attenuates frequencies above cutoff`() {
        val sut = SpectrumManager(config.copy(lowPassFilter = lowPassConfig))

        val bins = sut.calculate(combinedWavesFrame)

        val peak1 = bins.nearestTo(frequency1)
        val peak2 = bins.nearestTo(frequency2)

        assertEquals(1f, peak1.magnitude, 0.1f)
        assertEquals(0.1f, peak2.magnitude, 0.1f, "Expected $frequency2 Hz to be attenuated")
    }

    @Test
    fun `bins above low pass threshold are not included`() {
        val sut = SpectrumManager(config.copy(lowPassFilter = lowPassConfig))

        val bins = sut.calculate(wave1Frame)

        val threshold = lowPassConfig.frequencyAt(config.rolloffThreshold)
        val highestBin = bins.maxBy { it.frequency }
        assertTrue(
            highestBin.frequency <= threshold,
            "Expected no bins above low pass threshold of $threshold Hz"
        )
    }

    // Optimization
    @Test
    fun `given a low pass filter is used, performance is optimized by decimating`() {
        val decimator = spyk(Decimator())
        val sut = SpectrumManager(
            config = config.copy(lowPassFilter = lowPassConfig),
            decimator = decimator
        )

        sut.calculate(wave1Frame)

        verify { decimator.decimate(any(), any(), any(), any()) }
    }

}