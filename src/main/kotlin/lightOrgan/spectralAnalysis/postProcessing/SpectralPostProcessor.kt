package lightOrgan.spectralAnalysis.postProcessing

import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.noiseReduction.NoiseReducer
import lightOrgan.spectralAnalysis.noiseReduction.SpectralGate

class SpectralPostProcessor(
    private val noiseReducer: NoiseReducer = SpectralGate(),
) {

    fun processSpectrum(spectrum: FrequencyBins, config: SpectralAnalysisConfig): FrequencyBins {
        return spectrum
            .filter { it.frequency in config.audioConditioner.passband }
            .let { noiseReducer.reduceSpectrum(it, config.noiseReduction) }
    }

    fun processPeaks(peaks: SpectralPeaks, config: SpectralAnalysisConfig): SpectralPeaks {
        return peaks
            .filter { it.frequency in config.audioConditioner.passband }
            .let { noiseReducer.reducePeaks(it, config.noiseReduction) }
    }

}