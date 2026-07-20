package toolkit.monkeyTest

import kotlin.random.Random

fun nextFloat(min: Float = 1f, max: Float = 1024f): Float {
    require(min < max) { "min must be less than max" }

    return Random
        .nextDouble(min.toDouble(), max.toDouble())
        .toFloat()
}

fun nextPositiveFloat(min: Float = 1f, max: Float = 1024f): Float {
    require(min >= 0f) { "min must not be negative" }

    return nextFloat(min, max)
}

fun nextNegativeFloat(min: Float = -1024f, max: Float = -1f): Float {
    require(max <= 0f) { "max must not be positive" }

    return nextFloat(min, max)
}