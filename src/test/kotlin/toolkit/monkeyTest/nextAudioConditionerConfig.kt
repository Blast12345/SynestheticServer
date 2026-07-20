package toolkit.monkeyTest

import lightOrgan.spectralAnalysis.conditioning.AudioConditionerConfig
import kotlin.random.Random

fun nextAudioConditionerConfig(): AudioConditionerConfig {
    return AudioConditionerConfig(
        gainDb = nextPositiveFloat(),
        highPassFilter = nextHighPassConfig(),
        lowPassFilter = nextLowPassConfig(),
        rolloffThresholdDb = nextPositiveFloat(),
        decimate = Random.nextBoolean()
    )
}