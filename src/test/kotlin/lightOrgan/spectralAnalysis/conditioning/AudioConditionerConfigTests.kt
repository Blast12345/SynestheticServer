package lightOrgan.spectralAnalysis.conditioning

import dsp.filtering.FilterConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.minimalAppConfig
import toolkit.monkeyTest.nextPositiveFloat

class AudioConditionerConfigTests {

    private val minimalConfig = minimalAppConfig.spectralAnalysis.audioConditioner

    private val threshold = nextPositiveFloat() // TODO: Negative
    private val highPassFilter: FilterConfig.HighPass = mockk()
    private val lowPassFilter: FilterConfig.LowPass = mockk()

    private val lowerFrequency = nextPositiveFloat()
    private val higherFrequency = nextPositiveFloat()

    @BeforeEach
    fun setupHappyPath() {
        every { highPassFilter.frequencyAt(threshold) } returns lowerFrequency
        every { lowPassFilter.frequencyAt(threshold) } returns higherFrequency
    }

    // Passband
    @Test
    fun `given no rolloff threshold, the everything is in the passband`() {
        val sut = minimalConfig.copy(
            rolloffThresholdDb = null,
            highPassFilter = highPassFilter,
            lowPassFilter = lowPassFilter
        )

        assertEquals(0f, sut.passband.lowerFrequency)
        assertEquals(Float.POSITIVE_INFINITY, sut.passband.higherFrequency)
    }

    @Test
    fun `given a rolloff threshold and a high pass filter, the passband's lower frequency is the filter's frequency at that threshold`() {
        val sut = minimalConfig.copy(
            rolloffThresholdDb = threshold,
            highPassFilter = highPassFilter
        )

        assertEquals(lowerFrequency, sut.passband.lowerFrequency)
    }

    @Test
    fun `given a rolloff threshold but no high pass filter, the passband's lower frequency is zero`() {
        val sut = minimalConfig.copy(
            rolloffThresholdDb = threshold,
            highPassFilter = null,
            lowPassFilter = lowPassFilter
        )

        assertEquals(0f, sut.passband.lowerFrequency)
    }

    @Test
    fun `given a rolloff threshold and a low pass filter, the passband's higher frequency is the filter's frequency at that threshold`() {
        val sut = minimalConfig.copy(
            rolloffThresholdDb = threshold,
            lowPassFilter = lowPassFilter
        )

        assertEquals(higherFrequency, sut.passband.higherFrequency)
    }

    @Test
    fun `given a rolloff threshold but no low pass filter, the passband's higher frequency is positive infinity`() {
        val sut = minimalConfig.copy(
            rolloffThresholdDb = threshold,
            highPassFilter = highPassFilter,
            lowPassFilter = null
        )

        assertEquals(Float.POSITIVE_INFINITY, sut.passband.higherFrequency)
    }

}