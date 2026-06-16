package extensions

import org.apache.commons.math3.complex.Complex

operator fun Complex.plus(other: Complex): Complex = add(other)
operator fun Complex.minus(other: Complex): Complex = subtract(other)
operator fun Complex.times(other: Complex): Complex = multiply(other)
operator fun Complex.times(scalar: Double): Complex = multiply(scalar)
operator fun Complex.div(scalar: Double): Complex = divide(scalar)