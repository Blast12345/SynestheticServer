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
    private val lowPassFilter: FilterConfig.LowPass = mockk()
    private val lowPassFrequencyAtThreshold = nextPositiveFloat()

    @BeforeEach
    fun setupHappyPath() {
        every { lowPassFilter.frequencyAt(threshold) } returns lowPassFrequencyAtThreshold
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