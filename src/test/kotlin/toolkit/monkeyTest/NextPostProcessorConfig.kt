package toolkit.monkeyTest

import lightOrgan.spectralAnalysis.postProcessing.PostProcessorConfig

fun nextPostProcessorConfig(): PostProcessorConfig {
    return PostProcessorConfig(
        noiseReducer = nextNoiseReducerConfig()
    )
}