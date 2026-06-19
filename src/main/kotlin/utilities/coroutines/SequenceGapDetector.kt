package utilities.coroutines

sealed class SequenceCheck {
    data object Ok : SequenceCheck()
    data class Gap(val size: Long) : SequenceCheck()
    data object Reset : SequenceCheck()
}

class SequenceGapDetector {

    private var expectedSequenceNumber: Long? = null

    fun check(sequenceNumber: Long): SequenceCheck {
        val expected = expectedSequenceNumber

        val result = when {
            expected == null -> SequenceCheck.Ok
            sequenceNumber < expected -> SequenceCheck.Reset
            sequenceNumber > expected -> SequenceCheck.Gap(sequenceNumber - expected)
            else -> SequenceCheck.Ok
        }

        expectedSequenceNumber = sequenceNumber + 1

        return result
    }

}