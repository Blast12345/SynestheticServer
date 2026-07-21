package toolkit.monkeyTest

import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculatorConfig

fun nextSpectrumCalculatorConfig(): SpectrumCalculatorConfig {
    return SpectrumCalculatorConfig(
        frameDuration = nextDuration(),
        approximateBinSpacing = nextPositiveFloat(),
        window = nextEnum<WindowType>(),
    )
}