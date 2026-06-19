package lightOrgan.color

import color.*
import dsp.peakExtraction.SpectralPeak
import dsp.peakExtraction.SpectralPeaks
import dsp.peakExtraction.combinedMagnitude
import math.normalization.UnitInterval
import math.perception.StevensPowerLaw
import math.physics.Light
import music.TuningSystem
import music.WesternTuningSystem
import kotlin.time.Duration.Companion.milliseconds

class ColorWheelAlgorithm(
    private val tuning: TuningSystem = WesternTuningSystem(),
    private val lightSmoother: Smoother<Light> = Smoothers.lightExponentialMovingAverage(50.milliseconds),
    private val brightnessSmoother: Smoother<Double> = Smoothers.envelopeFollower(attackHalfLife = 0.milliseconds, releaseHalfLife = 20.milliseconds),
    private val chromaticityFactory: ChromaticityFactory = ChromaticityFactory(),
) : ColorAlgorithm {

    private var chromaticity: Chromaticity = Chromaticity.Achromatic

    override fun calculate(spectralPeaks: SpectralPeaks): StandardRgbColor {
        // think of each peak as its own light source
        val lights = spectralPeaks.map { createLight(it) }

        // we point the lights at a virtual wall and mix them together
        val combinedLight = lights.fold(Light()) { sum, current -> sum + current }
        val smoothedLight = lightSmoother.smooth(combinedLight)

        // we can calculate the chromaticity - color, independent of brightness
        // when the sound stops, we hold its last color so the brightness release fades through it
        chromaticityFactory.fromLight(smoothedLight)?.let { chromaticity = it }

        // we then calculate the overall loudness of the sound
        val subjectiveLoudness = StevensPowerLaw.LOUDNESS_3KHZ_TONE.perceivedIntensity(spectralPeaks.combinedMagnitude)
        val smoothedBrightness = brightnessSmoother.smooth(subjectiveLoudness)
        val brightness = UnitInterval.clamped(smoothedBrightness)

        // finally, we create a color using the hue and saturation of the combined light
        // and make it as bright as the sound is loud
        return HsbColor.from<StandardRGB>(chromaticity, brightness).toRgb()
    }

    private fun createLight(spectralPeak: SpectralPeak): Light {
        // HSB is conventionally a perceptual color space, but light is combined in linear space.
        val colorForHue = HsbColor<StandardRGB>(
            hue = tuning.getPositionInOctave(spectralPeak.frequency),
            saturation = UnitInterval.one,
            brightness = UnitInterval.one
        ).toRgb().toLinear()

        // If the input has an equal loudness contour applied, then a Stevens Power Law of 3 kHz should apply cleanly to all frequencies.
        // If no equal loudness contour is applied, then this is still the most straightforward way of achieving perceived loudness.
        val subjectiveLoudness = StevensPowerLaw.LOUDNESS_3KHZ_TONE.perceivedIntensity(spectralPeak.magnitude.toDouble())

        // We percieve the final combination of light, not the individual components, so we mix using objective brightness (i.e. model real world physics)
        // 5° extended-field is the Stevens condition matching a steadily-lit wall (vs. point sources or brief flashes).
        val objectiveBrightness = StevensPowerLaw.BRIGHTNESS_5DEG_IN_DARK.stimulusIntensity(subjectiveLoudness)

        return Light(
            colorForHue.red.value,
            colorForHue.green.value,
            colorForHue.blue.value,
        ).times(objectiveBrightness)
    }

}