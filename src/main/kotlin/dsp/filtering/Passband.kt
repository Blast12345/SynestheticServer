package dsp.filtering

data class Passband(
    val lowerFrequency: Float,
    val higherFrequency: Float,
) {

    companion object {
        val ALL = Passband(0f, Float.POSITIVE_INFINITY)
    }

    operator fun contains(frequency: Float): Boolean {
        return frequency in lowerFrequency..higherFrequency
    }

}