package lightOrgan.spectralAnalysis

import dsp.filtering.FilterConfig
import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import kotlin.time.Duration

data class SpectralAnalysisConfig(
    val gainDb: Float,  // e.g. 12 dBFS
    val frameDuration: Duration,
    val approximateBinSpacing: Float,
    val rolloffThreshold: Float, // e.g. -48 dBFS
    val highPassFilter: FilterConfig.HighPass?,
    val lowPassFilter: FilterConfig.LowPass?,
    val window: WindowType,
    val peakExtractor: PeakExtractorConfig,
    val decimate: Boolean
)