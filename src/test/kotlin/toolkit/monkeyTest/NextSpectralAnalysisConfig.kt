package toolkit.monkeyTest

import lightOrgan.spectralAnalysis.PostProcessorConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig

fun nextSpectralAnalysisConfig(): SpectralAnalysisConfig {
    return SpectralAnalysisConfig(
        audioConditioner = nextAudioConditionerConfig(),
        spectrumCalculator = nextSpectrumCalculatorConfig(),
        peakExtractor = nextPeakExtractorConfig(),
        postProcessor = nextPostProcessorConfig()
    )
}

fun nextPostProcessorConfig(): PostProcessorConfig {
    return PostProcessorConfig(
        noiseReducer = nextNoiseReducerConfig()
    )
}