package dsp.bins

import math.magnitude
import org.apache.commons.math3.complex.Complex
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

data class FrequencyBin(
    val frequency: Float,
    val value: Complex
) {

    // I'm using the term "magnitude" instead of "amplitude" because the value is inherently non-negative.
    // Reference: https://dsp.stackexchange.com/questions/8317/fft-amplitude-or-magnitude
    val magnitude = value.magnitude.toFloat()

    constructor(frequency: Float, magnitude: Double, phase: Double = 0.0) : this(
        frequency = frequency,
        value = Complex(magnitude * cos(phase), magnitude * sin(phase))
    )

    override fun toString(): String = "FrequencyBin(frequency=$frequency, magnitude=${magnitude})"

}

typealias FrequencyBins = List<FrequencyBin>

fun FrequencyBins.nearestTo(frequency: Float): FrequencyBin {
    return minBy { abs(it.frequency - frequency) }
}
