package lightOrgan.spectralAnalyzer

import audio.samples.AudioFrame
import dsp.filtering.Filter
import dsp.filtering.FilterBuilder
import dsp.filtering.FilterConfig
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import lightOrgan.spectralAnalyzer.spectrum.FilterManager
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import toolkit.monkeyTest.*

class FilterManagerTests {

    private val highPassFilterConfig = nextHighPassConfig()
    private val lowPassFilterConfig = nextLowPassConfig()
    private val filterBuilder: FilterBuilder = mockk()

    private val mockHighPass: Filter = mockk()
    private val highPassedSamples = nextFloatArray()
    private val mockLowPass: Filter = mockk()
    private val lowPassedSamples = nextFloatArray()

    private val format1 = nextAudioFormat()
    private val format2 = nextAudioFormat()
    private val format1Frame1 = nextAudioFrame(format = format1)
    private val format1Frame2 = nextAudioFrame(format = format1)
    private val format2Frame1 = nextAudioFrame(format = format2)

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

        val result = sut.filter(format1Frame1)

        assertEquals(format1Frame1, result)
    }

    @Test
    fun `given a high pass filter is defined, audio is high pass filtered`() {
        val sut = createSUT(highPassFilterConfig, null)

        val result = sut.filter(format1Frame1)

        val expected = AudioFrame(highPassedSamples, format1)
        assertEquals(expected, result)
    }

    @Test
    fun `given a low pass filter is defined, audio is low pass filtered`() {
        val sut = createSUT(null, lowPassFilterConfig)

        val result = sut.filter(format1Frame1)

        val expected = AudioFrame(lowPassedSamples, format1)
        assertEquals(expected, result)
    }

    @Test
    fun `given both filters are defined, audio is filtered by both`() {
        val sut = createSUT(highPassFilterConfig, lowPassFilterConfig)

        sut.filter(format1Frame1)

        verify { mockHighPass.filter(any()) }
        verify { mockLowPass.filter(any()) }
        // Ordering doesn't matter, so we cannot reasonably verify the result without implying an order
    }

    // Statefulness
    @Test
    fun `filters are reused when sample rate does not change`() {
        val sut = createSUT(highPassFilterConfig, lowPassFilterConfig)

        sut.filter(format1Frame1)
        sut.filter(format1Frame2)

        verify(exactly = 1) { filterBuilder.build(highPassFilterConfig, format1.sampleRate) }
        verify(exactly = 1) { filterBuilder.build(lowPassFilterConfig, format1.sampleRate) }
    }

    @Test
    fun `filters are rebuilt when sample rate changes`() {
        val sut = createSUT(highPassFilterConfig, lowPassFilterConfig)

        sut.filter(format1Frame1)
        sut.filter(format2Frame1)

        verify(exactly = 1) { filterBuilder.build(highPassFilterConfig, format1.sampleRate) }
        verify(exactly = 1) { filterBuilder.build(highPassFilterConfig, format2.sampleRate) }
        verify(exactly = 1) { filterBuilder.build(lowPassFilterConfig, format1.sampleRate) }
        verify(exactly = 1) { filterBuilder.build(lowPassFilterConfig, format2.sampleRate) }
    }

}