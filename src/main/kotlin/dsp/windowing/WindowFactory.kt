package dsp.windowing

import annotations.SkipCoverage

@SkipCoverage
class WindowFactory {

    // Plug in the appropriate coefficients to add new window types.
    // Reference: https://en.wikipedia.org/wiki/Window_function
    fun create(type: WindowType): Window {
        return when (type) {
            WindowType.Hann -> GeneralizedCosineWindow(floatArrayOf(0.5f, 0.5f))
            WindowType.BlackmanHarris3Term -> GeneralizedCosineWindow(floatArrayOf(0.42438f, 0.49734f, 0.07828f))
            WindowType.BlackmanHarris4Term -> GeneralizedCosineWindow(floatArrayOf(0.35875f, 0.48829f, 0.14128f, 0.01168f))
        }
    }

}