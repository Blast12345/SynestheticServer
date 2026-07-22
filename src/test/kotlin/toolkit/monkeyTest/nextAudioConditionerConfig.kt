package toolkit.monkeyTest

import lightOrgan.spectralAnalysis.conditioning.AudioConditionerConfig

fun nextAudioConditionerConfig(): AudioConditionerConfig {
    return AudioConditionerConfig(
        gainDb = nextPositiveFloat(),
        highPassFilter = nextHighPassConfig(),
        lowPassFilter = nextLowPassConfig(),
        rolloffThresholdDb = nextNegativeFloat(),
        decimation = nextDecimationConfig(),
    )
}