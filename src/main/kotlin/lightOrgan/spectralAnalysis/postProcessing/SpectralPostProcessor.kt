package lightOrgan.spectralAnalysis.postProcessing

import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks
import lightOrgan.spectralAnalysis.noiseReduction.NoiseReducerFactory

class SpectralPostProcessor(
    private val noiseReducerFactory: NoiseReducerFactory = NoiseReducerFactory()
) {

    fun processSpectrum(spectrum: FrequencyBins, config: PostProcessorConfig): FrequencyBins {
        var processed = spectrum

        if (config.noiseReducer != null) {
            val noiseReducer = noiseReducerFactory.create(config.noiseReducer)
            processed = noiseReducer.reduceSpectrum(processed)
        }

        return processed
    }

    fun processPeaks(peaks: SpectralPeaks, config: PostProcessorConfig): SpectralPeaks {
        var processed = peaks

        if (config.noiseReducer != null) {
            val noiseReducer = noiseReducerFactory.create(config.noiseReducer)
            processed = noiseReducer.reducePeaks(processed)
        }

        return processed
    }

}