package utilities.coroutines

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextInt

class MapSequencedIntegrationTests {

    private val number1 = nextInt()
    private val number2 = nextInt()

    // Normal operation
    @Test
    fun `transform the inner value`() = runTest {
        val result = flowOf(number1.asSequenced(0L))
            .mapSequenced { it * 2 }
            .first()

        assertEquals(number1 * 2, result.value)
    }

    @Test
    fun `outgoing sequence is independent of incoming`() = runTest {
        val results = flowOf(number1.asSequenced(10L), number2.asSequenced(11L))
            .mapSequenced { it }
            .toList()

        assertEquals(
            listOf(Sequenced(0L, number1), Sequenced(1L, number2)),
            results
        )
    }

    // Gap
    @Test
    fun `report when gaps occur`() = runTest {
        val gaps = mutableListOf<Long>()

        flowOf(number1.asSequenced(0), number2.asSequenced(2))
            .mapSequenced(transform = { it }, onGap = { gaps.add(it) })
            .toList()

        assertEquals(listOf(1L), gaps)
    }

    // Reset
    @Test
    fun `report when reset occurs`() = runTest {
        var resetCount = 0

        flowOf(number1.asSequenced(0), number2.asSequenced(0))
            .mapSequenced(onReset = { resetCount++ }, transform = { it })
            .toList()

        assertEquals(1, resetCount)
    }

    @Test
    fun `outgoing sequence continues through upstream reset`() = runTest {
        val results = flowOf(
            number1.asSequenced(0),
            number2.asSequenced(1),
            number1.asSequenced(0),
            number2.asSequenced(1)
        )
            .mapSequenced(transform = { it })
            .toList()

        assertEquals(
            listOf(
                Sequenced(0L, number1),
                Sequenced(1L, number2),
                Sequenced(2L, number1),
                Sequenced(3L, number2)
            ),
            results
        )
    }

}