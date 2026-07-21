package lightOrgan.spectralAnalysis

import lightOrgan.spectralAnalysis.conditioning.AudioConditionerConfig
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculatorConfig

data class SpectralAnalysisConfig(
    val audioConditioner: AudioConditionerConfig,
    val spectrumCalculator: SpectrumCalculatorConfig,
    val peakExtractor: PeakExtractorConfig
)