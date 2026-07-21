package toolkit.monkeyTest

import lightOrgan.spectralAnalysis.SpectralAnalysisConfig

fun nextSpectralAnalysisConfig(): SpectralAnalysisConfig {
    return SpectralAnalysisConfig(
        audioConditioner = nextAudioConditionerConfig(),
        spectrumCalculator = nextSpectrumCalculatorConfig(),
        peakExtractor = nextPeakExtractorConfig()
    )
}