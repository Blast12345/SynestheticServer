package dsp.clean

data class CleanConfiguration(
    val loopGain: Double,
    val maxIterations: Int,
    val magnitudeThreshold: Double
)