package lightOrgan.spectralAnalysis.noiseReduction

import annotations.SkipCoverage
import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks

interface NoiseReducer {
    sealed interface Config

    fun reduceSpectrum(spectrum: FrequencyBins): FrequencyBins
    fun reducePeaks(peaks: SpectralPeaks): SpectralPeaks
}

@SkipCoverage
class NoiseReducerFactory {

    fun create(config: NoiseReducer.Config): NoiseReducer {
        return when (config) {
            is SpectralGateConfig -> SpectralGate(config)
        }
    }

}