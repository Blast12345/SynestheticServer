package lightOrgan.color

import color.StandardRgbColor
import color.StandardRgbColors
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lightOrgan.spectralAnalyzer.SpectralAnalysis

// ENHANCEMENT: OKLCH (or other perceptually balanced spectrum)
// ENHANCEMENT: Force a given hue, saturation, or color.
class ColorManager(
    private val colorAlgorithm: ColorAlgorithm = ColorWheelAlgorithm(),
) {

    private val _color = MutableStateFlow(StandardRgbColors.Black)
    val color: StateFlow<StandardRgbColor> = _color.asStateFlow()

    fun calculate(spectralAnalysis: SpectralAnalysis): StandardRgbColor {
        _color.value = colorAlgorithm.calculate(spectralAnalysis.peaks)
        return _color.value
    }

}