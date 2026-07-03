package lightOrgan.spectralAnalysis.noiseReduction

import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks

interface NoiseReducer {
    fun reduce(spectrum: FrequencyBins): FrequencyBins
    fun reduce(peaks: SpectralPeaks): SpectralPeaks
}