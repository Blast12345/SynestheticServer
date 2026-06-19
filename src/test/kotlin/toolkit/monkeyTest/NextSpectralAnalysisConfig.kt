package toolkit.monkeyTest

import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import kotlin.random.Random

fun nextSpectralAnalysisConfig(): SpectralAnalysisConfig {
    return SpectralAnalysisConfig(
        gainDb = nextPositiveFloat(),
        frameDuration = nextDuration(),
        approximateBinSpacing = nextPositiveFloat(),
        rolloffThreshold = nextPositiveFloat(),
        highPassFilter = nextHighPassConfig(),
        lowPassFilter = nextLowPassConfig(),
        window = nextEnum<WindowType>(),
        peakExtractor = nextPeakExtractorConfig(),
        decimate = Random.nextBoolean()
    )
}