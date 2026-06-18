package math.geometry

@JvmInline
value class Angle private constructor(
    val radians: Double
) {

    // TODO: Test
    init {
        require(!radians.isNaN()) {
            "Angle radians must not be NaN"
        }
        require(radians.isFinite()) {
            "Angle radians must be finite, was $radians"
        }
    }

    val degrees: Double get() = Math.toDegrees(radians)
    val turns: Double get() = radians / (2 * Math.PI)
    val normalized: Angle get() = fromTurns(turns.mod(1.0))

    companion object {
        val zero = Angle(0.0)

        fun fromRadians(value: Double) = Angle(value)
        fun fromDegrees(value: Double) = Angle(Math.toRadians(value))
        fun fromTurns(value: Double) = Angle(value * 2 * Math.PI)
    }

}