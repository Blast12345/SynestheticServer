package lightOrgan.spectralAnalysis.peaks

import dsp.bins.FrequencyBins
import dsp.peakExtraction.CleanPeakExtractor
import dsp.peakExtraction.ParabolicSpectralPeakExtractor
import dsp.peakExtraction.SpectralPeakExtractor
import dsp.peakExtraction.SpectralPeaks
import lightOrgan.spectralAnalysis.spectrum.PointSpreadFunction

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