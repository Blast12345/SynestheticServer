package dsp.windowing

import kotlin.math.sqrt

abstract class Window {

    enum class CorrectionType { MAGNITUDE, ENERGY, NONE }

    private val cache = mutableMapOf<Int, FloatArray>()

    protected abstract fun computeCoefficients(size: Int): FloatArray

    fun coefficients(size: Int): FloatArray {
        return cache.getOrPut(size) { computeCoefficients(size) }
    }

    fun magnitudeCorrectionFactor(sampleSize: Int): Float {
        val coefficients = coefficients(sampleSize)
        return coefficients.size / coefficients.sum()
    }

    fun energyCorrectionFactor(sampleSize: Int): Float {
        val coefficients = coefficients(sampleSize)
        return sqrt(coefficients.size / coefficients.map { it * it }.sum())
    }

    fun appliedTo(frame: FloatArray, correction: CorrectionType = CorrectionType.NONE): FloatArray {
        val coefficients = coefficients(frame.size)

        val correctionFactor = when (correction) {
            CorrectionType.MAGNITUDE -> magnitudeCorrectionFactor(frame.size)
            CorrectionType.ENERGY -> energyCorrectionFactor(frame.size)
            CorrectionType.NONE -> 1f
        }

        return FloatArray(frame.size) { index -> coefficients[index] * frame[index] * correctionFactor }
    }

}