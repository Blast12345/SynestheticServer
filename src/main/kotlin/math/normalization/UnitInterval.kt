package math.normalization

// Reference: https://math.stackexchange.com/questions/1626783/is-there-a-name-for-numbers-between-0-and-1
@JvmInline
value class UnitInterval(val value: Double) {

    init {
        require(value in 0.0..1.0) { "Unit interval must be in [0.0, 1.0], got $value" }
    }

    companion object {
        val zero = UnitInterval(0.0)
        val one = UnitInterval(1.0)

        fun clamped(raw: Double): UnitInterval = UnitInterval(raw.coerceIn(0.0, 1.0))
    }

}