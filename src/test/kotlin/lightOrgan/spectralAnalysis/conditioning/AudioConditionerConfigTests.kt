package lightOrgan.spectralAnalysis.conditioning

import dsp.filtering.FilterConfig
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import toolkit.minimalAppConfig
import toolkit.monkeyTest.nextNegativeFloat
import toolkit.monkeyTest.nextPositiveFloat

class AudioConditionerConfigTests {

    private val minimalConfig = minimalAppConfig.spectralAnalysis.audioConditioner


    private val threshold = nextNegativeFloat()
    private val highPassFilter: FilterConfig.HighPass = mockk()
    private val lowPassFilter: FilterConfig.LowPass = mockk()

    private val lowerFrequency = nextPositiveFloat()
    private val lowPassFrequencyAtThreshold = nextPositiveFloat()

    @BeforeEach
    fun setupHappyPath() {
        every { highPassFilter.frequencyAt(threshold) } returns lowerFrequency
        every { lowPassFilter.frequencyAt(threshold) } returns lowPassFrequencyAtThreshold
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

        assertEquals(lowPassFrequencyAtThreshold, sut.passband.higherFrequency)
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

    // Decimation Frequency
    @Test
    fun `given no decimation config, decimation frequency is null`() {
        val sut = minimalConfig.copy(decimation = null)

        assertNull(sut.decimationFrequency)
    }

    @Test
    fun `given explicit decimation, decimation frequency is the specified frequency`() {
        val frequency = nextPositiveFloat()
        val sut = minimalConfig.copy(decimation = DecimationConfig.Explicit(frequency))

        assertEquals(frequency, sut.decimationFrequency)
    }

    @Test
    fun `given automatic decimation with a low pass filter, decimation frequency is the filter's frequency at the threshold`() {
        val sut = minimalConfig.copy(
            decimation = DecimationConfig.Automatic(threshold),
            lowPassFilter = lowPassFilter
        )

        assertEquals(lowPassFrequencyAtThreshold, sut.decimationFrequency)
    }

    @Test
    fun `given automatic decimation without a low pass filter, decimation frequency is null`() {
        val sut = minimalConfig.copy(
            decimation = DecimationConfig.Automatic(threshold),
            lowPassFilter = null
        )

        assertNull(sut.decimationFrequency)
    }

}