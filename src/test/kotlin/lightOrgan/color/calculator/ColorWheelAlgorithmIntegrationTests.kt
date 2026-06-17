package lightOrgan.color.calculator

import dsp.peakExtraction.SpectralPeak
import lightOrgan.color.ColorWheelAlgorithm
import lightOrgan.color.Smoother
import lightOrgan.color.Smoothers
import math.perception.StevensPowerLaw
import math.physics.Light
import music.WesternTuningSystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.math.pow

class ColorWheelAlgorithmIntegrationTests {

    private val westernTuning = WesternTuningSystem()
    private val cFrequency = westernTuning.getFrequency(westernTuning.C, octave = 4)
    private val dFrequency = westernTuning.getFrequency(westernTuning.D, octave = 4)
    private val fSharpFrequency = westernTuning.getFrequency(westernTuning.F_SHARP, octave = 4)
    private val halfLoudness = loudnessToMagnitude(0.5)
    private val fullLoudness = loudnessToMagnitude(1.0)

    private val redPeak = SpectralPeak(cFrequency, fullLoudness)
    private val cyanPeak = SpectralPeak(fSharpFrequency, fullLoudness)

    private val alwaysGreen = Smoother<Light> { Light(0.0, 1.0, 0.0) }
    private val halvingBrightness = Smoother<Double> { it * 0.5 }
    private val constantBrightness = Smoother<Double> { 1.0 }

    private fun loudnessToMagnitude(loudness: Double): Float {
        return loudness.pow(1.0 / StevensPowerLaw.LOUDNESS_3KHZ_TONE.exponent).toFloat()
    }

    private fun createSUT(
        lightSmoother: Smoother<Light> = Smoothers.none(),
        brightnessSmoother: Smoother<Double> = Smoothers.none(),
    ): ColorWheelAlgorithm {
        return ColorWheelAlgorithm(
            tuning = westernTuning,
            lightSmoother = lightSmoother,
            brightnessSmoother = brightnessSmoother
        )
    }

    // Silence
    @Test
    fun `given no peaks, then black is returned`() {
        val sut = createSUT()

        val actual = sut.calculate(listOf())

        assertEquals(0.0, actual.red.value, 0.001)
        assertEquals(0.0, actual.green.value, 0.001)
        assertEquals(0.0, actual.blue.value, 0.001)
    }


    // Full loudness tones
    // Reference: https://en.wikipedia.org/wiki/Color_wheel#/media/File:RGB_color_wheel_with_hue_and_hex.svg
    @Test
    fun `given a C note at full loudness, then red is returned`() {
        val sut = createSUT()
        val actual = sut.calculate(listOf(SpectralPeak(cFrequency, fullLoudness)))
        assertEquals(1.0, actual.red.value, 0.001)
        assertEquals(0.0, actual.green.value, 0.001)
        assertEquals(0.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given a C# note at full loudness, then orange is returned`() {
        val sut = createSUT()
        val frequency = westernTuning.getFrequency(westernTuning.C_SHARP, octave = 4)
        val actual = sut.calculate(listOf(SpectralPeak(frequency, fullLoudness)))
        assertEquals(1.0, actual.red.value, 0.001)
        assertEquals(0.5, actual.green.value, 0.001)
        assertEquals(0.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given a D note at full loudness, then yellow is returned`() {
        val sut = createSUT()
        val actual = sut.calculate(listOf(SpectralPeak(dFrequency, fullLoudness)))
        assertEquals(1.0, actual.red.value, 0.001)
        assertEquals(1.0, actual.green.value, 0.001)
        assertEquals(0.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given a D# note at full loudness, then chartreuse is returned`() {
        val sut = createSUT()
        val frequency = westernTuning.getFrequency(westernTuning.D_SHARP, octave = 4)
        val actual = sut.calculate(listOf(SpectralPeak(frequency, fullLoudness)))
        assertEquals(0.5, actual.red.value, 0.001)
        assertEquals(1.0, actual.green.value, 0.001)
        assertEquals(0.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given an E note at full loudness, then green is returned`() {
        val sut = createSUT()
        val frequency = westernTuning.getFrequency(westernTuning.E, octave = 4)
        val actual = sut.calculate(listOf(SpectralPeak(frequency, fullLoudness)))
        assertEquals(0.0, actual.red.value, 0.001)
        assertEquals(1.0, actual.green.value, 0.001)
        assertEquals(0.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given an F note at full loudness, then spring green is returned`() {
        val sut = createSUT()
        val frequency = westernTuning.getFrequency(westernTuning.F, octave = 4)
        val actual = sut.calculate(listOf(SpectralPeak(frequency, fullLoudness)))
        assertEquals(0.0, actual.red.value, 0.001)
        assertEquals(1.0, actual.green.value, 0.001)
        assertEquals(0.5, actual.blue.value, 0.001)
    }

    @Test
    fun `given an F# note at full loudness, then cyan is returned`() {
        val sut = createSUT()
        val actual = sut.calculate(listOf(SpectralPeak(fSharpFrequency, fullLoudness)))
        assertEquals(0.0, actual.red.value, 0.001)
        assertEquals(1.0, actual.green.value, 0.001)
        assertEquals(1.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given a G note at full loudness, then azure is returned`() {
        val sut = createSUT()
        val frequency = westernTuning.getFrequency(westernTuning.G, octave = 4)
        val actual = sut.calculate(listOf(SpectralPeak(frequency, fullLoudness)))
        assertEquals(0.0, actual.red.value, 0.001)
        assertEquals(0.5, actual.green.value, 0.001)
        assertEquals(1.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given a G# note at full loudness, then blue is returned`() {
        val sut = createSUT()
        val frequency = westernTuning.getFrequency(westernTuning.G_SHARP, octave = 4)
        val actual = sut.calculate(listOf(SpectralPeak(frequency, fullLoudness)))
        assertEquals(0.0, actual.red.value, 0.001)
        assertEquals(0.0, actual.green.value, 0.001)
        assertEquals(1.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given an A note at full loudness, then violet is returned`() {
        val sut = createSUT()
        val frequency = westernTuning.getFrequency(westernTuning.A, octave = 4)
        val actual = sut.calculate(listOf(SpectralPeak(frequency, fullLoudness)))
        assertEquals(0.5, actual.red.value, 0.001)
        assertEquals(0.0, actual.green.value, 0.001)
        assertEquals(1.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given an A# note at full loudness, then magenta is returned`() {
        val sut = createSUT()
        val frequency = westernTuning.getFrequency(westernTuning.A_SHARP, octave = 4)
        val actual = sut.calculate(listOf(SpectralPeak(frequency, fullLoudness)))
        assertEquals(1.0, actual.red.value, 0.001)
        assertEquals(0.0, actual.green.value, 0.001)
        assertEquals(1.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given a B note at full loudness, then rose is returned`() {
        val sut = createSUT()
        val frequency = westernTuning.getFrequency(westernTuning.B, octave = 4)
        val actual = sut.calculate(listOf(SpectralPeak(frequency, fullLoudness)))
        assertEquals(1.0, actual.red.value, 0.001)
        assertEquals(0.0, actual.green.value, 0.001)
        assertEquals(0.5, actual.blue.value, 0.001)
    }

    // Half loudness tones
    @Test
    fun `given a C note at half loudness, then red at half brightness is returned`() {
        val sut = createSUT()
        val peak = SpectralPeak(cFrequency, halfLoudness)

        val actual = sut.calculate(listOf(peak))

        assertEquals(0.5, actual.red.value, 0.001)
        assertEquals(0.0, actual.green.value, 0.001)
        assertEquals(0.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given a F# note at half loudness, then cyan at half brightness is returned`() {
        val sut = createSUT()
        val peak = SpectralPeak(fSharpFrequency, halfLoudness)

        val actual = sut.calculate(listOf(peak))

        assertEquals(0.0, actual.red.value, 0.001)
        assertEquals(0.5, actual.green.value, 0.001)
        assertEquals(0.5, actual.blue.value, 0.001)
    }

    // Mixed tones
    @Test
    fun `given C and F# notes each at full loudness, then white at full brightness is returned`() {
        val sut = createSUT()
        val peak1 = SpectralPeak(cFrequency, fullLoudness)
        val peak2 = SpectralPeak(fSharpFrequency, fullLoudness)

        val actual = sut.calculate(listOf(peak1, peak2))

        // we can't have a brightness greater than 1, so clip
        assertEquals(1.0, actual.red.value, 0.001)
        assertEquals(1.0, actual.green.value, 0.001)
        assertEquals(1.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given C and D notes each at full loudness, then orange at full brightness is returned`() {
        val sut = createSUT()
        val peak1 = SpectralPeak(cFrequency, fullLoudness) // C is red
        val peak2 = SpectralPeak(dFrequency, fullLoudness) // D is yellow

        val actual = sut.calculate(listOf(peak1, peak2)) // They should arrive at orange

        assertEquals(1.0, actual.red.value, 0.001)
        assertEquals(0.735, actual.green.value, 0.001)
        assertEquals(0.0, actual.blue.value, 0.001)
    }

    @Test
    fun `given C and F# notes each at half loudness, then white at slightly greater than half brightness is returned`() {
        val sut = createSUT()
        val peak1 = SpectralPeak(cFrequency, halfLoudness)
        val peak2 = SpectralPeak(fSharpFrequency, halfLoudness)

        val actual = sut.calculate(listOf(peak1, peak2))

        // analogous to a ~3 dB gain
        assertEquals(0.630, actual.red.value, 0.001)
        assertEquals(0.630, actual.green.value, 0.001)
        assertEquals(0.630, actual.blue.value, 0.001)
    }

    // Smoothing
    @Test
    fun `light smoother determines hue and saturation`() {
        val sut = createSUT(lightSmoother = alwaysGreen)

        val color1 = sut.calculate(listOf(redPeak))
        assertEquals(0.0, color1.red.value, 0.001)
        assertEquals(1.0, color1.green.value, 0.001)
        assertEquals(0.0, color1.blue.value, 0.001)

        val color2 = sut.calculate(listOf(cyanPeak))
        assertEquals(0.0, color2.red.value, 0.001)
        assertEquals(1.0, color2.green.value, 0.001)
        assertEquals(0.0, color2.blue.value, 0.001)
    }

    @Test
    fun `brightness smoother determines brightness`() {
        val sut = createSUT(brightnessSmoother = halvingBrightness)

        val actual = sut.calculate(listOf(redPeak))

        assertEquals(0.5, actual.red.value, 0.001)
        assertEquals(0.0, actual.green.value, 0.001)
        assertEquals(0.0, actual.blue.value, 0.001)
    }

    @Test
    fun `while brightness remains, the last chromaticity is held`() {
        val sut = createSUT(lightSmoother = Smoothers.none(), brightnessSmoother = constantBrightness)

        // Set the initial color to red
        val color1 = sut.calculate(listOf(redPeak))

        assertEquals(1.0, color1.red.value, 0.001)
        assertEquals(0.0, color1.green.value, 0.001)
        assertEquals(0.0, color1.blue.value, 0.001)

        // Silence results in an undefined chromaticity, so use the last known chromaticity (i.e. red hue)
        val color2 = sut.calculate(listOf())

        assertEquals(1.0, color2.red.value, 0.001)
        assertEquals(0.0, color2.green.value, 0.001)
        assertEquals(0.0, color2.blue.value, 0.001)
    }

    @Test
    fun `when sound resumes, the new chromaticity replaces the held one`() {
        val sut = createSUT(lightSmoother = Smoothers.none(), brightnessSmoother = constantBrightness)

        // Establish a chromaticity
        val firstColor = sut.calculate(listOf(redPeak))

        assertEquals(1.0, firstColor.red.value, 0.001)
        assertEquals(0.0, firstColor.green.value, 0.001)
        assertEquals(0.0, firstColor.blue.value, 0.001)

        // Then let silence put the hold in effect
        sut.calculate(listOf())

        // A new note arrives
        val resumedColor = sut.calculate(listOf(cyanPeak))

        assertEquals(0.0, resumedColor.red.value, 0.001)
        assertEquals(1.0, resumedColor.green.value, 0.001)
        assertEquals(1.0, resumedColor.blue.value, 0.001)
    }

}