package lightOrgan.spectralAnalysis.postProcessing

import dsp.bins.FrequencyBins
import dsp.filtering.Passband
import dsp.peakExtraction.SpectralPeaks
import lightOrgan.spectralAnalysis.noiseReduction.NoiseReducer
import lightOrgan.spectralAnalysis.noiseReduction.NoiseReducerFactory

class SpectralPostProcessor(
    private val noiseReducerFactory: NoiseReducerFactory = NoiseReducerFactory()
) {

    fun processSpectrum(spectrum: FrequencyBins, passband: Passband, noiseReductionConfig: NoiseReducer.Config?): FrequencyBins {
        var processed = spectrum.filter { it.frequency in passband }

        if (noiseReductionConfig != null) {
            val noiseReducer = noiseReducerFactory.create(noiseReductionConfig)
            processed = noiseReducer.reduceSpectrum(processed)
        }

        return processed
    }

    fun processPeaks(peaks: SpectralPeaks, passband: Passband, noiseReductionConfig: NoiseReducer.Config?): SpectralPeaks {
        var processed = peaks.filter { it.frequency in passband }

        if (noiseReductionConfig != null) {
            val noiseReducer = noiseReducerFactory.create(noiseReductionConfig)
            processed = noiseReducer.reducePeaks(processed)
        }

        return processed
    }

}