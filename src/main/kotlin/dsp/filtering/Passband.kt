package dsp.filtering

data class Passband(
    val lowerFrequency: Float,
    val higherFrequency: Float,
) {

    operator fun contains(frequency: Float): Boolean {
        return frequency in lowerFrequency..higherFrequency
    }

}