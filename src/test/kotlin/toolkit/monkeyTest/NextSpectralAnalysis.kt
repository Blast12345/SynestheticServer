package toolkit.monkeyTest

import lightOrgan.spectralAnalysis.SpectralAnalysis

fun nextSpectralAnalysis(): SpectralAnalysis {
    return SpectralAnalysis(
        spectrum = nextFrequencyBins(),
        peaks = nextSpectralPeaks()
    )
}
