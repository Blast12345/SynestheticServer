package toolkit.monkeyTest

import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.AudioConditionerConfig
import lightOrgan.spectralAnalysis.NoiseReductionConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.SpectrumCalculatorConfig
import kotlin.random.Random

fun nextSpectralAnalysisConfig(): SpectralAnalysisConfig {
    return SpectralAnalysisConfig(
        audioConditioner = nextAudioConditionerConfig(),
        spectrumCalculator = nextSpectrumCalculatorConfig(),
        peakExtractor = nextPeakExtractorConfig(),
        noiseReduction = nextNoiseReductionConfig()
    )
}

fun nextAudioConditionerConfig(): AudioConditionerConfig {
    return AudioConditionerConfig(
        gainDb = nextPositiveFloat(),
        highPassFilter = nextHighPassConfig(),
        lowPassFilter = nextLowPassConfig(),
        rolloffThreshold = nextPositiveFloat(),
        decimate = Random.nextBoolean()
    )
}

fun nextSpectrumCalculatorConfig(): SpectrumCalculatorConfig {
    return SpectrumCalculatorConfig(
        frameDuration = nextDuration(),
        approximateBinSpacing = nextPositiveFloat(),
        window = nextEnum<WindowType>(),
    )
}

fun nextNoiseReductionConfig(): NoiseReductionConfig {
    return NoiseReductionConfig(
        thresholdDb = Random.nextDouble(),
        kneeWidthDb = Random.nextDouble()
    )
}