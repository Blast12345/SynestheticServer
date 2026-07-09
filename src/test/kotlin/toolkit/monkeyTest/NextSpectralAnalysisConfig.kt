package toolkit.monkeyTest

import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.AudioConditionerConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.SpectrumCalculatorConfig
import lightOrgan.spectralAnalysis.noiseReduction.NoiseReducer
import lightOrgan.spectralAnalysis.noiseReduction.SpectralGate
import kotlin.random.Random

fun nextSpectralAnalysisConfig(): SpectralAnalysisConfig {
    return SpectralAnalysisConfig(
        audioConditioner = nextAudioConditionerConfig(),
        spectrumCalculator = nextSpectrumCalculatorConfig(),
        peakExtractor = nextPeakExtractorConfig(),
        noiseReducer = nextNoiseReducerConfig()
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

// TODO: Move me
fun nextNoiseReducerConfig(): NoiseReducer.Config {
    return listOf(
        nextSpectralGateConfig()
    ).random()
}

fun nextSpectralGateConfig(): SpectralGate.Config {
    return SpectralGate.Config(
        thresholdDb = Random.nextDouble()
    )
}