package dsp

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import toolkit.monkeyTest.nextPositiveFloat

class DecimatorTests {

    private val monoAudio = AudioFrame(
        floatArrayOf(1f, 2f, 3f, 4f, 5f, 6f), // [S1, S2, S3, S4, S5, S6]
        AudioFormat(sampleRate = 48000f, channels = 1, bitDepth = 0)
    )

    private val stereoAudio = AudioFrame(
        floatArrayOf(1f, 10f, 2f, 20f, 3f, 30f, 4f, 40f), // [L1, R1, L2, R2, L3, L4, R4]
        AudioFormat(sampleRate = 48000f, channels = 2, bitDepth = 0)
    )

    private val monoAudioPart1 = AudioFrame(
        floatArrayOf(1f, 2f, 3f), // [S1, S2, S3]
        monoAudio.format
    )

    private val monoAudioPart2 = AudioFrame(
        floatArrayOf(4f, 5f, 6f), // [S4, S5, S6]
        monoAudio.format
    )

    // Decimation factor
    @Test
    fun `decimation factor is the ratio of sample rate to target nyquist rate`() {
        val sut = Decimator()

        assertEquals(1, sut.decimationFactor(48000f, 24000f))
        assertEquals(2, sut.decimationFactor(48000f, 12000f))
        assertEquals(4, sut.decimationFactor(48000f, 6000f))
    }

    @Test
    fun `throw when the target nyquist frequency exceeds sample rate capability`() {
        val sut = Decimator()

        assertThrows<IllegalArgumentException> {
            sut.decimationFactor(48000f, 100000f)
        }
    }

    // Decimation
    @Test
    fun `given a factor of 1, samples are returned unchanged`() {
        val sut = Decimator()

        val result = sut.decimate(monoAudio, factor = 1)

        assertArrayEquals(monoAudio.samples, result.samples)
        assertEquals(monoAudio.format, result.format)
    }

    @Test
    fun `given a factor of 2, every other sample is kept`() {
        val sut = Decimator()

        val result = sut.decimate(monoAudio, factor = 2)

        val expected = floatArrayOf(1f, 3f, 5f) // [S1, S3, S5]
        assertArrayEquals(expected, result.samples)
        assertEquals(monoAudio.format.sampleRate / 2, result.format.sampleRate)
    }

    @Test
    fun `given multichannel audio, decimation keeps channels aligned`() {
        val sut = Decimator()

        val result = sut.decimate(stereoAudio, factor = 2)

        val expected = floatArrayOf(1f, 10f, 3f, 30f) // [L1, R1, L3, R3]
        assertArrayEquals(expected, result.samples)
    }

    // Decimation state
    @Test
    fun `phase is maintained across consecutive calls`() {
        val sut = Decimator()

        val result1 = sut.decimate(monoAudioPart1, factor = 2)
        val result2 = sut.decimate(monoAudioPart2, factor = 2)

        // Processing as one block: [S1, S2, S3, S4, S5, S6] with factor 2 → [S1, S3, S5]
        // Split across calls, should produce the same total output
        val combined = result1.samples + result2.samples
        val expected = floatArrayOf(1f, 3f, 5f) // [S1, S3, S5]
        assertArrayEquals(expected, combined)
    }

    @Test
    fun `phase resets when sample rate changes`() {
        val sut = Decimator()
        val part2DifferentRate = monoAudioPart2.copy(format = monoAudio.format.copy(sampleRate = nextPositiveFloat()))

        sut.decimate(monoAudioPart1, factor = 2)
        val result2 = sut.decimate(part2DifferentRate, factor = 2)

        val expected = floatArrayOf(4f, 6f) // [S4, S6]
        assertArrayEquals(expected, result2.samples)
    }

    @Test
    fun `phase resets when factor changes`() {
        val sut = Decimator()

        sut.decimate(monoAudioPart1, factor = 2)
        val result2 = sut.decimate(monoAudioPart2, factor = 3)

        val expected = floatArrayOf(4f) // [S4]
        assertArrayEquals(expected, result2.samples)
    }

    @Test
    fun `phase resets when channel count changes`() {
        val sut = Decimator()

        sut.decimate(monoAudioPart1, factor = 2)
        val result2 = sut.decimate(stereoAudio, factor = 2)

        val expected = floatArrayOf(1f, 10f, 3f, 30f) // [L1, R1, L3, R3]
        assertArrayEquals(expected, result2.samples)
    }

    @Test
    fun `given chunk is smaller than decimation stride, phase carries across without output`() {
        val sut = Decimator()

        val chunk1 = AudioFrame(floatArrayOf(1f, 2f, 3f), monoAudio.format)
        val chunk2 = AudioFrame(floatArrayOf(4f, 5f, 6f), monoAudio.format)
        val chunk3 = AudioFrame(floatArrayOf(7f, 8f, 9f), monoAudio.format)
        val chunk4 = AudioFrame(floatArrayOf(10f, 11f, 12f), monoAudio.format)

        // result3 has no output because we have COMPLETELY stepped over it given the factor.
        // But we still need to keep track of the phase for the next set of samples.
        val result1 = sut.decimate(chunk1, factor = 5)
        val result2 = sut.decimate(chunk2, factor = 5)
        val result3 = sut.decimate(chunk3, factor = 5)
        val result4 = sut.decimate(chunk4, factor = 5)

        val combined = result1.samples + result2.samples + result3.samples + result4.samples
        assertArrayEquals(floatArrayOf(1f, 6f, 11f), combined)
    }

    // Decimation - invalid input
    @Test
    fun `given a factor less than 1, then throw`() {
        val sut = Decimator()

        assertThrows<IllegalArgumentException> {
            sut.decimate(monoAudio, factor = 0)
        }

        assertThrows<IllegalArgumentException> {
            sut.decimate(monoAudio, factor = -1)
        }
    }

}