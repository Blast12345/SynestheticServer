package lightOrgan.spectralAnalysis

import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.conditioning.AudioConditionerConfig
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import kotlin.time.Duration

data class SpectralAnalysisConfig(
    val audioConditioner: AudioConditionerConfig,
    val frameDuration: Duration,
    val approximateBinSpacing: Float,
    val window: WindowType,
    val peakExtractor: PeakExtractorConfig
)