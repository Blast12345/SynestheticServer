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
    val peakExtractor: PeakExtractorConfig,
    val noiseReduction: NoiseReductionConfig,
)

data class AudioConditionerConfig(
    val gainDb: Float,
    val rolloffThreshold: Float, // e.g. -48 dBFS
    val highPassFilter: FilterConfig.HighPass?,
    val lowPassFilter: FilterConfig.LowPass?,
    val decimate: Boolean,
)

// ENHANCEMENT: As more reduction strategies are added, this config can become a sealed interface
data class NoiseReductionConfig(
    val thresholdDb: Double,
    val kneeWidthDb: Double,
) {

    init {
        require(kneeWidthDb >= 0.0) { "Noise reduction knee width cannot be negative." }
    }

}