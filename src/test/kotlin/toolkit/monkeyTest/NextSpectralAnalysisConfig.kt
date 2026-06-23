package toolkit.monkeyTest

import dsp.windowing.WindowType
import lightOrgan.spectralAnalysis.AudioConditionerConfig
import lightOrgan.spectralAnalysis.NoiseReductionConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import kotlin.random.Random

fun nextSpectralAnalysisConfig(): SpectralAnalysisConfig {
    return SpectralAnalysisConfig(
        audioConditioner = nextAudioConditionerConfig(),
        frameDuration = nextDuration(),
        approximateBinSpacing = nextPositiveFloat(),
        window = nextEnum<WindowType>(),
        peakExtractor = nextPeakExtractorConfig(),
        noiseReduction = nextNoiseReductionConfig()
    )
}

fun nextAudioConditionerConfig(): AudioConditionerConfig {
    return AudioConditionerConfig(
        gainDb = nextPositiveFloat(),
        highPassFilter = nextHighPassConfig(),
        lowPassFilter = nextLowPassConfig(),
        rolloffThreshold = nextPositiveFloat(),
        decimate = Random.nextBoolean()
    )
}

fun nextNoiseReductionConfig(): NoiseReductionConfig {
    return NoiseReductionConfig(
        threshold = Random.nextDouble(),
        kneeWidth = Random.nextDouble()
    )
}