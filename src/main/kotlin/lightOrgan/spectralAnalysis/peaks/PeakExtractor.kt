package lightOrgan.spectralAnalysis.peaks

import dsp.bins.FrequencyBins
import dsp.peakExtraction.ParabolicSpectralPeakExtractor
import dsp.peakExtraction.SpectralPeakExtractor
import dsp.peakExtraction.SpectralPeaks

// TODO: Test me
class PeakExtractor(
    private val extractor: SpectralPeakExtractor = ParabolicSpectralPeakExtractor()
) {

    fun extract(
        bins: FrequencyBins
    ): SpectralPeaks = when (extractor) {
        is ParabolicSpectralPeakExtractor -> extractor.extract(bins)
    }

}