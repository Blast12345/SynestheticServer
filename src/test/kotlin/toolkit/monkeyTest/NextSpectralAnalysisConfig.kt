package toolkit.monkeyTest

import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig

fun nextSpectralAnalysisConfig(): SpectralAnalysisConfig {
    return SpectralAnalysisConfig(
        audioConditioner = nextAudioConditionerConfig(),
        frameDuration = nextDuration(),
        approximateBinSpacing = nextPositiveFloat(),
        window = nextEnum<WindowType>(),
        peakExtractor = nextPeakExtractorConfig(),
    )
}