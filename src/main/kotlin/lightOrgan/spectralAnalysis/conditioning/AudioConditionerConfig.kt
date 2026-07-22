package lightOrgan.spectralAnalysis.conditioning

import dsp.filtering.FilterConfig
import dsp.filtering.Passband

// ENHANCEMENT: Warn about using explicit decimation risking aliasing
data class AudioConditionerConfig(
    val gainDb: Float,
    val highPassFilter: FilterConfig.HighPass?,
    val lowPassFilter: FilterConfig.LowPass?,
    val rolloffThresholdDb: Float?,
    val decimation: DecimationConfig?
) {

    val passband: Passband
        get() {
            val threshold = rolloffThresholdDb ?: return Passband.ALL

            return Passband(
                lowerFrequency = highPassFilter?.frequencyAt(threshold) ?: 0f,
                higherFrequency = lowPassFilter?.frequencyAt(threshold) ?: Float.POSITIVE_INFINITY,
            )
        }

    val decimationFrequency: Float? = when (decimation) {
        is DecimationConfig.Automatic -> lowPassFilter?.frequencyAt(decimation.thresholdDb)
        is DecimationConfig.Explicit -> decimation.frequency
        null -> null
    }

}

sealed interface DecimationConfig {
    data class Automatic(val thresholdDb: Float) : DecimationConfig
    data class Explicit(val frequency: Float) : DecimationConfig
}