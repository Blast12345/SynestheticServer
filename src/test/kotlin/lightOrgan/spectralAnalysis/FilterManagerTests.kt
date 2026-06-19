package lightOrgan.spectralAnalysis

import dsp.filtering.Filter
import dsp.filtering.FilterBuilder
import dsp.filtering.FilterConfig
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import lightOrgan.spectralAnalysis.spectrum.FilterManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import toolkit.monkeyTest.nextFloatArray
import toolkit.monkeyTest.nextHighPassConfig
import toolkit.monkeyTest.nextLowPassConfig
import kotlin.random.Random.Default.nextFloat

class FilterManagerTests {

    private val highPassFilterConfig = nextHighPassConfig()
    private val lowPassFilterConfig = nextLowPassConfig()
    private val filterBuilder: FilterBuilder = mockk()

    private val mockHighPass: Filter = mockk()
    private val highPassedSamples = nextFloatArray()
    private val mockLowPass: Filter = mockk()
    private val lowPassedSamples = nextFloatArray()

    private val sampleRate1 = nextFloat()
    private val sampleRate2 = nextFloat()
    private val samples1 = nextFloatArray()
    private val samples2 = nextFloatArray()

    @BeforeEach
    fun setupHappyPath() {
        every { filterBuilder.build(highPassFilterConfig, any()) } returns mockHighPass
        every { filterBuilder.build(lowPassFilterConfig, any()) } returns mockLowPass

        every { mockHighPass.filter(any()) } returns highPassedSamples
        every { mockLowPass.filter(any()) } returns lowPassedSamples
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(
        highPass: FilterConfig? = null,
        lowPass: FilterConfig? = null,
        filterBuilder: FilterBuilder = this.filterBuilder
    ) = FilterManager(highPass, lowPass, filterBuilder)


    // Construction
    @Test
    fun `the high pass filter must be a high pass filter`() {
        assertThrows<IllegalArgumentException> {
            createSUT(highPass = lowPassFilterConfig)
        }
    }

    @Test
    fun `the low pass filter must be a low pass filter`() {
        assertThrows<IllegalArgumentException> {
            createSUT(lowPass = highPassFilterConfig)
        }
    }

    // Filtering
    @Test
    fun `given no filters are defined, then return the audio unchanged`() {
        val sut = createSUT(highPass = null, lowPass = null)

        val result = sut.filter(samples1, sampleRate1)

        assertEquals(samples1, result)
    }

    @Test
    fun `given a high pass filter is defined, audio is high pass filtered`() {
        val sut = createSUT(highPassFilterConfig, null)

        val result = sut.filter(samples1, sampleRate1)

        assertEquals(highPassedSamples, result)
    }

    @Test
    fun `given a low pass filter is defined, audio is low pass filtered`() {
        val sut = createSUT(null, lowPassFilterConfig)

        val result = sut.filter(samples1, sampleRate1)

        assertEquals(lowPassedSamples, result)
    }

    @Test
    fun `given both filters are defined, audio is filtered by both`() {
        val sut = createSUT(highPassFilterConfig, lowPassFilterConfig)

        sut.filter(samples1, sampleRate1)

        verify { mockHighPass.filter(any()) }
        verify { mockLowPass.filter(any()) }
    }

    // Statefulness
    @Test
    fun `filters are reused when sample rate does not change`() {
        val sut = createSUT(highPassFilterConfig, lowPassFilterConfig)

        sut.filter(samples1, sampleRate1)
        sut.filter(samples2, sampleRate1)

        verify(exactly = 1) { filterBuilder.build(highPassFilterConfig, sampleRate1) }
        verify(exactly = 1) { filterBuilder.build(lowPassFilterConfig, sampleRate1) }
    }

    @Test
    fun `filters are rebuilt when sample rate changes`() {
        val sut = createSUT(highPassFilterConfig, lowPassFilterConfig)

        sut.filter(samples1, sampleRate1)
        sut.filter(samples2, sampleRate2)

        verify(exactly = 1) { filterBuilder.build(highPassFilterConfig, sampleRate1) }
        verify(exactly = 1) { filterBuilder.build(highPassFilterConfig, sampleRate2) }
        verify(exactly = 1) { filterBuilder.build(lowPassFilterConfig, sampleRate1) }
        verify(exactly = 1) { filterBuilder.build(lowPassFilterConfig, sampleRate2) }
    }

}