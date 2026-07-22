package dsp.windowing

// Plug in the appropriate coefficients to add new window types.
// Reference: https://en.wikipedia.org/wiki/Window_function
enum class WindowType(val cosineCoefficients: FloatArray) {
    Hann(floatArrayOf(0.5f, 0.5f)),
    BlackmanHarris3Term(floatArrayOf(0.42438f, 0.49734f, 0.07828f)),
    BlackmanHarris4Term(floatArrayOf(0.35875f, 0.48829f, 0.14128f, 0.01168f));
}