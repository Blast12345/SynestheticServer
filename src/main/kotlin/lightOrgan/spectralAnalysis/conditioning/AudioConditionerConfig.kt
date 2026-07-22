package lightOrgan.spectralAnalysis.conditioning

import dsp.filtering.FilterConfig

data class AudioConditionerConfig(
    val gainDb: Float,
    val highPassFilter: FilterConfig.HighPass?,
    val lowPassFilter: FilterConfig.LowPass?,
    val decimation: DecimationConfig?
) {


    val decimationFrequency: Float? = when (decimation) {
        is DecimationConfig.Automatic -> lowPassFilter?.frequencyAt(decimation.thresholdDb)
        is DecimationConfig.Explicit -> decimation.frequency
        null -> null
    }

}

// TODO:
sealed interface DecimationConfig {
    data class Automatic(val thresholdDb: Float) : DecimationConfig
    data class Explicit(val frequency: Float) : DecimationConfig
}