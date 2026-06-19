package lightOrgan.spectralAnalysis.peaks

sealed interface PeakExtractorConfig {
    data object Parabolic : PeakExtractorConfig
}