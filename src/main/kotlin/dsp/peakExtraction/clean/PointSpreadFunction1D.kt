package dsp.clean

import org.apache.commons.math3.complex.Complex

class PointSpreadFunction1D(
    val values: List<Complex>,
    val center: Int
) {
    init {
        require(values.isNotEmpty()) { "Values must not be empty" }
        require(center in values.indices) { "Center must be a valid index" }
    }
}