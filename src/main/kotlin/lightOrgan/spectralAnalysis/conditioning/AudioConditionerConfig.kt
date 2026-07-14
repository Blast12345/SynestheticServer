package lightOrgan.spectralAnalysis.conditioning

import dsp.filtering.FilterConfig
import dsp.filtering.Passband

data class AudioConditionerConfig(
    val gainDb: Float,
    val rolloffThresholdDb: Float?, // e.g. -48 dBFS
    val highPassFilter: FilterConfig.HighPass?,
    val lowPassFilter: FilterConfig.LowPass?,
    val decimate: Boolean,
) {

    val passband: Passband
        get() {
            val threshold = rolloffThresholdDb ?: return Passband.ALL

            return Passband(
                lowerFrequency = highPassFilter?.frequencyAt(threshold) ?: 0f,
                higherFrequency = lowPassFilter?.frequencyAt(threshold) ?: Float.POSITIVE_INFINITY,
            )
        }

}