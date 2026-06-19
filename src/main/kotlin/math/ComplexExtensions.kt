package math

import org.apache.commons.math3.complex.Complex

val Complex.magnitude: Double
    get() = abs()