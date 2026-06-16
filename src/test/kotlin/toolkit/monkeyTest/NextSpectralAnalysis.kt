package toolkit.monkeyTest

import lightOrgan.spectrum.SpectralAnalysis

fun nextSpectralAnalysis(): SpectralAnalysis {
    return SpectralAnalysis(
        spectrum = nextFrequencyBins(),
        peaks = nextSpectralPeaks()
    )
}
