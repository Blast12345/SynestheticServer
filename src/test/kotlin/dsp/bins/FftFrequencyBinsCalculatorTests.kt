//package dsp.bins
//
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.Assertions.assertTrue
//import org.junit.jupiter.api.Test
//import toolkit.generators.generateSilence
//import toolkit.generators.generateSineWave
//import kotlin.time.Duration.Companion.seconds
//
//class FftFrequencyBinsCalculatorTests {
//
//    private val frequency = 10f
//    private val sampleRate = 1024f // Any rate that supports the frequency would be valid
//    private val duration = 1.seconds // this gives us a 1 Hz resolution for easy testing
//
//    private val tone = generateSineWave(frequency, sampleRate = sampleRate, duration = duration)
//    private val silence = generateSilence(sampleRate, duration)
//
//    private fun createSUT() = FftFrequencyBinsCalculator()
//
//    // Frequency
//    @Test
//    fun `peak bin corresponds to the input frequency`() {
//        val sut = createSUT()
//
//        val bins = sut.calculate(tone.samples, sampleRate, 1f)
//
//        val peakBin = bins.maxBy { it.magnitude }
//        assertEquals(frequency, peakBin.frequency)
//    }
//
//    @Test
//    fun `bin frequencies are spaced by the frequency resolution`() {
//        val sut = createSUT()
//
//        val bins = sut.calculate(tone.samples, sampleRate, 1f)
//
//        val expectedSpacing = sampleRate / 2 / bins.size
//        bins.zipWithNext().forEach { (current, next) ->
//            assertEquals(expectedSpacing, next.frequency - current.frequency, 0.01f)
//        }
//    }
//
//    // Magnitude
//    @Test
//    fun `magnitudes are non-negative`() {
//        val sut = createSUT()
//
//        val bins = sut.calculate(tone.samples, sampleRate, 1f)
//
//        bins.forEach { assertTrue(it.magnitude >= 0f) }
//    }
//
//    @Test
//    fun `silence produces zero magnitudes`() {
//        val sut = createSUT()
//
//        val bins = sut.calculate(silence.samples, sampleRate, 1f)
//
//        bins.forEach { assertEquals(0f, it.magnitude, 0.001f) }
//    }
//
//    @Test
//    fun `magnitude correction factor scales magnitudes`() {
//        val sut = createSUT()
//        val correctionFactor = 2f
//
//        val uncorrected = sut.calculate(tone.samples, sampleRate, 1f)
//        val corrected = sut.calculate(tone.samples, sampleRate, correctionFactor)
//
//        uncorrected.zip(corrected).forEach { (base, scaled) ->
//            assertEquals(base.magnitude * correctionFactor, scaled.magnitude, 0.001f)
//        }
//    }
//
//}