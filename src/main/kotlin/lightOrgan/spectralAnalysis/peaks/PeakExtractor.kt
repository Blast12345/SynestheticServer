package lightOrgan.spectralAnalysis.peaks

import dsp.bins.FrequencyBins
import dsp.peakExtraction.*

// TODO: Test me
class PeakExtractor(
    private val extractor: SpectralPeakExtractor = CleanPeakExtractor()
) {

    fun extract(
        bins: FrequencyBins,
        pointSpreadFunction: PointSpreadFunction,
    ): SpectralPeaks = when (extractor) {
        is ParabolicSpectralPeakExtractor -> extractor.extract(bins)
        is CleanPeakExtractor -> extractor.extract(bins, pointSpreadFunction)
    }

}