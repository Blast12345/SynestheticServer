package lightOrgan.spectralAnalysis.noiseReduction

import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks
import lightOrgan.spectralAnalysis.NoiseReductionConfig

interface NoiseReducer {
    fun reduceSpectrum(spectrum: FrequencyBins, config: NoiseReductionConfig): FrequencyBins
    fun reducePeaks(peaks: SpectralPeaks, config: NoiseReductionConfig): SpectralPeaks
}