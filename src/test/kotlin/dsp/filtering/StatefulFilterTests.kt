package dsp.filtering

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextFloatArray
import toolkit.monkeyTest.nextHighPassConfig
import toolkit.monkeyTest.nextPositiveFloat

class StatefulFilterTests {

    private val config = nextHighPassConfig()
    private val filterBuilder: FilterBuilder = mockk()

    private val samples = nextFloatArray()
    private val sampleRate1 = nextPositiveFloat()
    private val sampleRate2 = nextPositiveFloat()

    private val filter1: Filter = mockk()
    private val filter1Samples = nextFloatArray()

    private val filter2: Filter = mockk()
    private val filter2Samples = nextFloatArray()

    @BeforeEach
    fun setupHappyPath() {
        every { filterBuilder.build(config, sampleRate1) } returns filter1
        every { filterBuilder.build(config, sampleRate2) } returns filter2

        every { filter1.filter(any()) } returns filter1Samples
        every { filter2.filter(any()) } returns filter2Samples
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT() = StatefulFilter(config, filterBuilder)

    @Test
    fun `filter samples`() {
        val sut = createSUT()

        val result = sut.filter(samples, sampleRate1)

        assertEquals(filter1Samples, result)
    }

    @Test
    fun `filter is reused when sample rate does not change`() {
        val sut = createSUT()

        sut.filter(samples, sampleRate1)
        sut.filter(nextFloatArray(), sampleRate1)

        verify(exactly = 1) { filterBuilder.build(config, sampleRate1) }
    }

    @Test
    fun `filter is rebuilt when sample rate changes`() {
        val sut = createSUT()

        sut.filter(samples, sampleRate1)
        val result = sut.filter(samples, sampleRate2)

        assertEquals(filter2Samples, result)
    }

    @Test
    fun `frequencyAt delegates to config`() {
        val sut = createSUT()
        val dBFS = nextPositiveFloat()

        val result = sut.frequencyAt(dBFS)

        assertEquals(config.frequencyAt(dBFS), result)
    }

}