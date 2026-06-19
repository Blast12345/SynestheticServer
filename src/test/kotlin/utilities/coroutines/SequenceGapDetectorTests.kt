package utilities.coroutines

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class SequenceGapDetectorTests {

    private fun createSUT(): SequenceGapDetector {
        return SequenceGapDetector()
    }

    // Ok
    @Test
    fun `first check is ok`() {
        val sut = createSUT()

        val result = sut.check(5L)

        assertEquals(SequenceCheck.Ok, result)
    }

    @Test
    fun `sequential numbers are ok`() {
        val sut = createSUT()
        sut.check(5L)

        val result = sut.check(6L)

        assertEquals(SequenceCheck.Ok, result)
    }

    // Gap
    @Test
    fun `when there is a gap in the ascending sequence, report the gap`() {
        val sut = createSUT()
        sut.check(0L)

        val result = sut.check(5L)

        assertEquals(SequenceCheck.Gap(4L), result)
    }

    @Test
    fun `after a gap, sequential numbers resume normally`() {
        val sut = createSUT()
        sut.check(0L)
        sut.check(5L)

        val result = sut.check(6L)

        assertEquals(SequenceCheck.Ok, result)
    }

    // Reset
    @Test
    fun `when there is a decrease in the sequence, report a reset`() {
        val sut = createSUT()
        sut.check(5L)

        val result = sut.check(1L)

        assertEquals(SequenceCheck.Reset, result)
    }

    @Test
    fun `after a reset, sequential numbers resume normally`() {
        val sut = createSUT()
        sut.check(5L)
        sut.check(0L)

        val result = sut.check(1L)

        assertEquals(SequenceCheck.Ok, result)
    }

}