package extensions

import org.apache.commons.math3.complex.Complex

operator fun Complex.times(scalar: Double): Complex = multiply(scalar)