package lightOrgan.color

import color.StandardRgbColor
import lightOrgan.spectralAnalysis.SpectralAnalysis

// ENHANCEMENT: OKLCH (or other perceptually balanced spectrum)
// ENHANCEMENT: Force a given hue, saturation, or color.
class ColorCalculator(
    private val colorAlgorithm: ColorAlgorithm = ColorWheelAlgorithm(),
) {

    fun calculate(spectralAnalysis: SpectralAnalysis): StandardRgbColor {
        return colorAlgorithm.calculate(spectralAnalysis.peaks)
    }

}