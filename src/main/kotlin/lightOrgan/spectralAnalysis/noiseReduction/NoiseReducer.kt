package lightOrgan.spectralAnalysis.noiseReduction

import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks

interface NoiseReducer {
    fun reduceSpectrum(spectrum: FrequencyBins): FrequencyBins
    fun reducePeaks(peaks: SpectralPeaks): SpectralPeaks
}