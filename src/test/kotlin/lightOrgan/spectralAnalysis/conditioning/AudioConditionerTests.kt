package lightOrgan.spectralAnalysis.conditioning

import dsp.Decimator
import dsp.Gain
import dsp.MonoMixer
import dsp.filtering.StatefulFilter
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.minimalAppConfig
import toolkit.monkeyTest.*

class AudioConditionerTests {

    private val minimalConfig = minimalAppConfig.spectralAnalysis.audioConditioner
    private val sampleRate = 48000f
    private val nyquistFrequency = sampleRate / 2
    private val frequencyBelowNyquist = nyquistFrequency / 2
    private val frequencyAboveNyquist = nyquistFrequency * 2

    private val highPassConfig = nextHighPassConfig()
    private val lowPassConfig = nextLowPassConfig()

    private val monoMixer: MonoMixer = mockk()
    private val gain: Gain = mockk()
    private val filterFactory: FilterFactory = mockk()
    private val highPassFilter: StatefulFilter = mockk()
    private val lowPassFilter: StatefulFilter = mockk()
    private val decimator: Decimator = mockk()

    private val stereoAudio = nextAudioFrame(format = nextAudioFormat(sampleRate, channels = 2))
    private val monoAudio = nextAudioFrame(format = nextAudioFormat(sampleRate, channels = 1))
    private val gainedSamples = nextFloatArray()
    private val highPassedSamples = nextFloatArray()
    private val lowPassedSamples = nextFloatArray()
    private val decimationFactor = nextInt()
    private val decimatedAudio = nextAudioFrame()

    @BeforeEach
    fun setupHappyPath() {
        every { filterFactory.create(highPassConfig) } returns highPassFilter
        every { filterFactory.create(lowPassConfig) } returns lowPassFilter

        every { monoMixer.mix(stereoAudio) } returns monoAudio
        every { gain.apply(monoAudio.samples, 3f) } returns gainedSamples
        every { highPassFilter.filter(monoAudio.samples, monoAudio.format.sampleRate) } returns highPassedSamples
        every { lowPassFilter.filter(monoAudio.samples, monoAudio.format.sampleRate) } returns lowPassedSamples
        every { decimator.decimationFactor(monoAudio.format.sampleRate, frequencyBelowNyquist) } returns decimationFactor
        every { decimator.decimate(monoAudio, decimationFactor) } returns decimatedAudio
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(): AudioConditioner {
        return AudioConditioner(
            monoMixer,
            gain,
            filterFactory,
            decimator
        )
    }

    // Mix to Mono
    @Test
    fun `downmix to mono`() {
        val sut = createSUT()

        val result = sut.condition(stereoAudio, minimalConfig)

        assertEquals(monoAudio, result)
    }

    // Gain
    @Test
    fun `apply gain`() {
        val sut = createSUT()
        val config = minimalConfig.copy(gainDb = 3f)

        val result = sut.condition(monoAudio, config)

        val expected = monoAudio.copy(samples = gainedSamples)
        assertEquals(expected, result)
    }

    // Filtering
    @Test
    fun `apply a high pass filter`() {
        val sut = createSUT()
        val config = minimalConfig.copy(highPassFilter = highPassConfig)

        val result = sut.condition(monoAudio, config)

        val expected = monoAudio.copy(samples = highPassedSamples)
        assertEquals(expected, result)
    }

    @Test
    fun `apply a low pass filter`() {
        val sut = createSUT()
        val config = minimalConfig.copy(lowPassFilter = lowPassConfig)

        val result = sut.condition(monoAudio, config)

        val expected = monoAudio.copy(samples = lowPassedSamples)
        assertEquals(expected, result)
    }

    // Decimation
    @Test
    fun `given a decimation target below Nyquist, apply decimation`() {
        val sut = createSUT()
        val config = minimalConfig.copy(decimation = DecimationConfig.Explicit(frequencyBelowNyquist))

        val result = sut.condition(monoAudio, config)

        assertEquals(decimatedAudio, result)
    }

    @Test
    fun `given a decimation target above Nyquist, do not decimate`() {
        val sut = createSUT()
        val config = minimalConfig.copy(decimation = DecimationConfig.Explicit(frequencyAboveNyquist))

        val result = sut.condition(monoAudio, config)

        assertEquals(monoAudio, result)
    }

}