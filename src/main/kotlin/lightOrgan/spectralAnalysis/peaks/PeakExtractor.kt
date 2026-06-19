package lightOrgan.spectralAnalysis.peaks

import dsp.bins.FrequencyBins
import dsp.peakExtraction.ParabolicSpectralPeakExtractor
import dsp.peakExtraction.SpectralPeakExtractor
import dsp.peakExtraction.SpectralPeaks
import lightOrgan.spectralAnalysis.spectrum.SpectralAnalyzerConfig

// TODO: Test me
// ENHANCEMENT: Reject peaks below the sidelobe dB? E.g. a peak of 0.5 yields sidelobes of X - then anything at X and below is removed
class PeakExtractor(
    private val config: SpectralAnalyzerConfig,
    private val extractor: SpectralPeakExtractor = ParabolicSpectralPeakExtractor()
) {

    fun extract(
        bins: FrequencyBins
    ): SpectralPeaks = when (extractor) {
        is ParabolicSpectralPeakExtractor -> extractor.extract(bins)
    }

}