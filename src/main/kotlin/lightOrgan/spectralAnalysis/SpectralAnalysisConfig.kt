package lightOrgan.spectralAnalysis

import dsp.filtering.FilterConfig
import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import kotlin.time.Duration

data class SpectralAnalysisConfig(
    val audioConditioner: AudioConditionerConfig,
    val frameDuration: Duration,
    val approximateBinSpacing: Float,
    val window: WindowType,
    val peakExtractor: PeakExtractorConfig
)

data class AudioConditionerConfig(
    val gainDb: Float,
    val rolloffThreshold: Float, // e.g. -48 dBFS
    val highPassFilter: FilterConfig.HighPass?,
    val lowPassFilter: FilterConfig.LowPass?,
    val decimate: Boolean,
)