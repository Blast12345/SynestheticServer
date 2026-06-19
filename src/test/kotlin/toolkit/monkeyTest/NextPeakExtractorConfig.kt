package toolkit.monkeyTest

import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig

fun nextPeakExtractorConfig(): PeakExtractorConfig {
    return listOf(
        nextParabolicExtractorConfig()
    ).random()
}

fun nextParabolicExtractorConfig(): PeakExtractorConfig.Parabolic {
    return PeakExtractorConfig.Parabolic
}