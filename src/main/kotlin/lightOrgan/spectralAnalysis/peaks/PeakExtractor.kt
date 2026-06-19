package lightOrgan.spectralAnalysis.peaks

import dsp.bins.FrequencyBins
import dsp.peakExtraction.ParabolicSpectralPeakExtractor
import dsp.peakExtraction.SpectralPeakExtractor
import dsp.peakExtraction.SpectralPeaks
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig

class PeakExtractor(
    private val config: SpectralAnalysisConfig,
    private val extractor: SpectralPeakExtractor = createExtractor(config.peakExtractor)
) {

    fun extract(
        spectrum: FrequencyBins
    ): SpectralPeaks = when (extractor) {
        is ParabolicSpectralPeakExtractor -> extractor.extract(spectrum)
    }

}

private fun createExtractor(type: PeakExtractorConfig) = when (type) {
    is PeakExtractorConfig.Parabolic -> ParabolicSpectralPeakExtractor()
}