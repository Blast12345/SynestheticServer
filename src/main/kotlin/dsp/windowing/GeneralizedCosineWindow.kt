package dsp.windowing

import kotlin.math.PI
import kotlin.math.cos

open class GeneralizedCosineWindow(
    private val cosineCoefficients: FloatArray
) : Window() {

    override fun computeCoefficients(size: Int): FloatArray {
        val lastIndex = size - 1
        return FloatArray(size) { index ->
            val phase = 2f * PI.toFloat() * index / lastIndex
            cosineCoefficients.foldIndexed(0f) { term, sum, coefficient ->
                val sign = if (term % 2 == 0) 1f else -1f
                sum + sign * coefficient * cos(term * phase)
            }
        }
    }

}