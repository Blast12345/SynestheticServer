package lightOrgan.spectralAnalysis.spectrum

import dsp.windowing.WindowType
import extensions.inSeconds
import kotlin.time.Duration

data class SpectrumCalculatorConfig(
    val frameDuration: Duration,
    val approximateBinSpacing: Float,
    val window: WindowType,
) {

    val frequencyResolution = 1 / frameDuration.inSeconds

}