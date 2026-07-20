package dsp.peakExtraction

import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt

data class SpectralPeak(
    val frequency: Float,
    val magnitude: Float
)

typealias SpectralPeaks = List<SpectralPeak>

fun List<SpectralPeak>.nearestTo(frequency: Float): SpectralPeak? = minByOrNull { abs(it.frequency - frequency) }

/**
 * Since peaks are at distinct frequencies, their energies sum directly.
 * We then take the square root of total energy to get back to magnitude.
 */
val SpectralPeaks.combinedMagnitude: Double
    get() {
        val totalEnergy = fold(0.0) { sum, peak -> sum + peak.magnitude.toDouble().pow(2) }
        return sqrt(totalEnergy)
    }