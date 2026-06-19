package dsp.filtering

import dsp.filtering.cascaded.butterworth.ButterworthHighPass
import dsp.filtering.cascaded.butterworth.ButterworthLowPass
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextFilterOrder

class FilterBuilderTests {

    private val sampleRate = 44100f
    private val frequency = 1000f
    private val order = nextFilterOrder()

    @Test
    fun `build a Butterworth low pass filter`() {
        val sut = FilterBuilder()

        val config = FilterConfig.LowPass(frequency, FilterFamily.Butterworth(order))

        val filter = sut.build(config, sampleRate)

        assertTrue(filter is ButterworthLowPass)
        assertEquals(sampleRate, filter.sampleRate)
        assertEquals(order.value, filter.order)
    }

    @Test
    fun `build a Butterworth high pass filter`() {
        val sut = FilterBuilder()

        val config = FilterConfig.HighPass(frequency, FilterFamily.Butterworth(order))

        val filter = sut.build(config, sampleRate)

        assertTrue(filter is ButterworthHighPass)
        assertEquals(sampleRate, filter.sampleRate)
        assertEquals(order.value, filter.order)
    }

}