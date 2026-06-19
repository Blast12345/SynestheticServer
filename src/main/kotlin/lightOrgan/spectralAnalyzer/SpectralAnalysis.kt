package lightOrgan.spectralAnalyzer

import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks

data class SpectralAnalysis(val spectrum: FrequencyBins, val peaks: SpectralPeaks) {
    companion object {
        val EMPTY = SpectralAnalysis(emptyList(), emptyList())
    }
}