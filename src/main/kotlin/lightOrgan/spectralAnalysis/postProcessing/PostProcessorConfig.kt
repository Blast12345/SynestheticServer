package lightOrgan.spectralAnalysis.postProcessing

import lightOrgan.spectralAnalysis.noiseReduction.NoiseReducer

data class PostProcessorConfig(
    val noiseReducer: NoiseReducer.Config?,
)