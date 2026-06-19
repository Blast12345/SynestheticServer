package dsp.filtering.cascaded

import dsp.filtering.cascaded.butterworth.ButterworthHighPass
import dsp.filtering.magnitudeToDb
import dsp.filtering.measureMagnitude
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import toolkit.generators.generateSineWave
import toolkit.monkeyTest.nextInt

class ButterworthHighPassTests {

    private val sampleRate = 44100f
    private val frequency = 1000f

    private val toneOneOctaveDown = generateSineWave(frequency / 2f, sampleRate = sampleRate)
    private val tone = generateSineWave(frequency, sampleRate = sampleRate)
    private val toneOneOctaveUp = generateSineWave(frequency * 2f, sampleRate = sampleRate)

    @Test
    fun `get the order of the filter`() {
        val order = nextInt()

        val filter = ButterworthHighPass(frequency, order, sampleRate)

        assertEquals(order, filter.order)
    }

    @Test
    fun `get the sample rate supported by the filter`() {
        val filter = ButterworthHighPass(frequency, order = 4, sampleRate)

        assertEquals(sampleRate, filter.sampleRate)
    }

    @Test
    fun `order must be at least 1`() {
        assertThrows<IllegalArgumentException> {
            ButterworthHighPass(frequency, order = 0, sampleRate)
        }
    }

    @Test
    fun `passes signal above cutoff`() {
        val filter = ButterworthHighPass(frequency, order = 4, sampleRate)

        val output = filter.filter(toneOneOctaveUp.waveForm.samples)

        val magnitude = measureMagnitude(output)
        assertEquals(1f, magnitude, 0.05f)
    }

    @Test
    fun `minus 3 dB at cutoff`() {
        val filter = ButterworthHighPass(frequency, order = 4, sampleRate)

        val output = filter.filter(tone.waveForm.samples)

        val magnitude = measureMagnitude(output)
        assertEquals(-3f, magnitudeToDb(magnitude), 0.5f)
    }

    @Test
    fun `rolls off at 6 dB per octave per order`() {
        val filter1 = ButterworthHighPass(frequency, order = 1, sampleRate)
        val output1 = filter1.filter(toneOneOctaveDown.waveForm.samples)
        val magnitude1 = measureMagnitude(output1)
        assertEquals(-6f, magnitudeToDb(magnitude1), 1.5f)

        val filter2 = ButterworthHighPass(frequency, order = 2, sampleRate)
        val output2 = filter2.filter(toneOneOctaveDown.waveForm.samples)
        val magnitude2 = measureMagnitude(output2)
        assertEquals(-12f, magnitudeToDb(magnitude2), 1.5f)
    }

}