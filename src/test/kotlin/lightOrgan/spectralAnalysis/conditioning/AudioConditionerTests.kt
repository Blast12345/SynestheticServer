package lightOrgan.spectralAnalysis.conditioning

import dsp.Decimator
import dsp.Gain
import dsp.MonoMixer
import dsp.filtering.StatefulFilter
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import lightOrgan.spectralAnalysis.AudioConditionerConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.*

class AudioConditionerTests {

    private val minimalConfig = AudioConditionerConfig(
        gainDb = 0f,
        highPassFilter = null,
        lowPassFilter = null,
        rolloffThreshold = -3f,
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

    private val lowPassConfig = nextLowPassConfig()
    private val lowPassFilter: StatefulFilter = mockk()
    private val lowPassStopFreq = lowPassConfig.frequencyAt(minimalConfig.rolloffThreshold!!)

    private val filteredSamples = nextFloatArray()

    @BeforeEach
    fun setupHappyPath() {
        every { monoMixer.mix(stereoAudio) } returns monoAudio
        every { gain.apply(monoAudio.samples, 3f) } returns gainedSamples

        every { filterFactory.create(highPassConfig) } returns highPassFilter
        every { highPassFilter.filter(monoAudio.samples, monoAudio.format.sampleRate) } returns filteredSamples

        every { filterFactory.create(lowPassConfig) } returns lowPassFilter
        every { lowPassFilter.filter(monoAudio.samples, monoAudio.format.sampleRate) } returns filteredSamples

        every { decimator.decimationFactor(monoAudio.format.sampleRate, lowPassStopFreq) } returns decimationFactor
        every { decimator.decimate(monoAudio.copy(samples = filteredSamples), decimationFactor) } returns decimatedAudio
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(config: AudioConditionerConfig = minimalConfig): AudioConditioner {
        return AudioConditioner(
            monoMixer,
            gain,
            filterFactory,
            decimator
        )
    }

    @Test
    fun `given multichannel audio, mix to mono`() {
        val sut = createSUT()

        val result = sut.condition(stereoAudio, minimalConfig)

        assertEquals(1, result.format.channels)
    }

    @Test
    fun `apply gain to audio`() {
        val sut = createSUT()

        val result = sut.condition(monoAudio, minimalConfig.copy(gainDb = 3f))

        val expected = monoAudio.copy(samples = gainedSamples)
        assertEquals(expected, result)
    }

    // Filtering
    @Test
    fun `apply high pass filter`() {
        val sut = createSUT()

        val result = sut.condition(monoAudio, minimalConfig.copy(highPassFilter = highPassConfig))

        val expected = monoAudio.copy(samples = filteredSamples)
        assertEquals(expected, result)
    }

    @Test
    fun `apply low pass filter`() {
        val sut = createSUT()

        val result = sut.condition(monoAudio, minimalConfig.copy(lowPassFilter = lowPassConfig))

        val expected = monoAudio.copy(samples = filteredSamples)
        assertEquals(expected, result)
    }

    // Decimation
    @Test
    fun `given a low pass filter, decimate to the the stopband`() {
        val sut = createSUT()

        val result = sut.condition(monoAudio, minimalConfig.copy(lowPassFilter = lowPassConfig, decimate = true))

        assertEquals(decimatedAudio, result)
    }

}