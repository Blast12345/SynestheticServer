package utilities.coroutines

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextString

class OnEachSequencedIntegrationTests {

    private val string1 = nextString("1")
    private val string2 = nextString("2")

    @Test
    fun `perform the action with the inner value`() = runTest {
        val received = mutableListOf<String>()

        flowOf(string1.asSequenced(0), string2.asSequenced(1))
            .onEachSequenced { received.add(it) }
            .toList()

        assertEquals(
            listOf(string1, string2),
            received
        )
    }

    @Test
    fun `report when gaps occur`() = runTest {
        val gaps = mutableListOf<Long>()

        flowOf(string1.asSequenced(0), string2.asSequenced(2))
            .onEachSequenced(action = {}, onGap = { gaps.add(it) })
            .toList()

        assertEquals(
            listOf(1L),
            gaps
        )
    }

    @Test
    fun `report when reset occurs`() = runTest {
        var resetCount = 0

        flowOf(string1.asSequenced(5), string2.asSequenced(0))
            .onEachSequenced(action = {}, onReset = { resetCount++ })
            .toList()

        assertEquals(1, resetCount)
    }

}