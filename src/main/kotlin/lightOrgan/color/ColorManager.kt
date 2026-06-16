package lightOrgan.color

import color.StandardRgbColor
import color.StandardRgbColors
import dsp.peakExtraction.SpectralPeaks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// ENHANCEMENT: OKLCH (or other perceptually balanced spectrum)
// ENHANCEMENT: Force a given hue, saturation, or color.
class ColorManager(
    private val colorAlgorithm: ColorAlgorithm = ColorWheelAlgorithm(),
) {

    private val _color = MutableStateFlow(StandardRgbColors.Black)
    val color: StateFlow<StandardRgbColor> = _color.asStateFlow()

    fun calculate(peaks: SpectralPeaks): StandardRgbColor {
        _color.value = colorAlgorithm.calculate(peaks)
        return _color.value
    }

}