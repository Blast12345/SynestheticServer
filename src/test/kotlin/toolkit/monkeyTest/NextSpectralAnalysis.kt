package toolkit.monkeyTest

import lightOrgan.spectralAnalyzer.SpectralAnalysis

fun nextSpectralAnalysis(): SpectralAnalysis {
    return SpectralAnalysis(
        spectrum = nextFrequencyBins(),
        peaks = nextSpectralPeaks()
    )
}
