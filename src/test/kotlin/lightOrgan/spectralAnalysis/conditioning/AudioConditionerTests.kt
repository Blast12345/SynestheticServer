package lightOrgan.spectralAnalysis.conditioning

import dsp.Decimator
import dsp.Gain
import dsp.MonoMixer
import dsp.filtering.StatefulFilter
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.*

class AudioConditionerTests {

    private val minimalConfig = nextSpectralAnalysisConfig().copy(
        gainDb = 0f,
        highPassFilter = null,
        lowPassFilter = null,
        decimate = false
    )

    private val monoMixer: MonoMixer = mockk()
    private val gain: Gain = mockk()
    private val filterFactory: FilterFactory = mockk()
    private val decimator: Decimator = mockk()

    private val stereoAudio = nextAudioFrame(format = nextAudioFormat(channels = 2))
    private val monoAudio = nextAudioFrame(format = nextAudioFormat(channels = 1))
    private val gainedSamples = nextFloatArray()
    private val decimationFactor = 4
    private val decimatedAudio = nextAudioFrame()

    private val highPassConfig = nextHighPassConfig()
    private val highPassFilter: StatefulFilter = mockk()
    private val lowerFrequency = 100f

    private val lowPassConfig = nextLowPassConfig()
    private val lowPassFilter: StatefulFilter = mockk()
    private val upperFrequency = 1000f

    private val filteredSamples = nextFloatArray()

    @BeforeEach
    fun setupHappyPath() {
        every { monoMixer.mix(stereoAudio) } returns monoAudio
        every { gain.apply(monoAudio.samples, 3f) } returns gainedSamples

        every { filterFactory.create(highPassConfig) } returns highPassFilter
        every { highPassFilter.frequencyAt(minimalConfig.rolloffThreshold) } returns lowerFrequency
        every { highPassFilter.filter(monoAudio.samples, monoAudio.format.sampleRate) } returns filteredSamples

        every { filterFactory.create(lowPassConfig) } returns lowPassFilter
        every { lowPassFilter.frequencyAt(minimalConfig.rolloffThreshold) } returns upperFrequency
        every { lowPassFilter.filter(monoAudio.samples, monoAudio.format.sampleRate) } returns filteredSamples

        every { decimator.decimationFactor(monoAudio.format.sampleRate, upperFrequency) } returns decimationFactor
        every { decimator.decimate(monoAudio.copy(samples = filteredSamples), decimationFactor) } returns decimatedAudio
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(config: SpectralAnalysisConfig = minimalConfig): AudioConditioner {
        return AudioConditioner(config, monoMixer, gain, filterFactory, decimator)
    }

    @Test
    fun `given multichannel audio, mix to mono`() {
        val sut = createSUT()

        val result = sut.condition(stereoAudio)

        assertEquals(1, result.format.channels)
    }

    @Test
    fun `apply gain to audio`() {
        val sut = createSUT(minimalConfig.copy(gainDb = 3f))

        val result = sut.condition(monoAudio)

        val expected = monoAudio.copy(samples = gainedSamples)
        assertEquals(expected, result)
    }

    // Filtering
    @Test
    fun `apply high pass filter`() {
        val sut = createSUT(minimalConfig.copy(highPassFilter = highPassConfig))

        val result = sut.condition(monoAudio)

        val expected = monoAudio.copy(samples = filteredSamples)
        assertEquals(expected, result)
    }

    @Test
    fun `apply low pass filter`() {
        val sut = createSUT(minimalConfig.copy(lowPassFilter = lowPassConfig))

        val result = sut.condition(monoAudio)

        val expected = monoAudio.copy(samples = filteredSamples)
        assertEquals(expected, result)
    }

    // Passband
    @Test
    fun `given a high pass filter, passband lower frequency is the rolloff`() {
        val sut = createSUT(minimalConfig.copy(highPassFilter = highPassConfig))
        assertEquals(lowerFrequency, sut.passband.lowerFrequency)
    }

    @Test
    fun `given no high pass filter, passband lower frequency is zero`() {
        val sut = createSUT(minimalConfig)
        assertEquals(0f, sut.passband.lowerFrequency)
    }

    @Test
    fun `given a low pass filter, passband upper frequency is the rolloff`() {
        val sut = createSUT(minimalConfig.copy(lowPassFilter = lowPassConfig))
        assertEquals(upperFrequency, sut.passband.higherFrequency)
    }

    @Test
    fun `given no low pass filter, passband upper frequency is infinity`() {
        val sut = createSUT(minimalConfig)
        assertEquals(Float.POSITIVE_INFINITY, sut.passband.higherFrequency)
    }

    // Decimation
    @Test
    fun `given a low pass filter, decimate to the the stopband`() {
        val sut = createSUT(minimalConfig.copy(lowPassFilter = lowPassConfig, decimate = true))

        val result = sut.condition(monoAudio)

        assertEquals(decimatedAudio, result)
    }

}