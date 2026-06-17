package lightOrgan.gateway

import color.rgb.StandardRgbColor
import jsonrpc.FakeJsonRpcConnection
import kotlinx.coroutines.test.runTest
import lightOrgan.gateway.models.BroadcastColor
import math.normalization.UnitInterval
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextGatewayDetails
import kotlin.time.Duration.Companion.milliseconds

class GatewayTests {

    private val details = nextGatewayDetails()
    private lateinit var fakeConnection: FakeJsonRpcConnection

    @BeforeEach
    fun setupHappyPath() {
        fakeConnection = FakeJsonRpcConnection()
    }

    private fun createSUT(): RealGateway {
        return RealGateway(
            details = details,
            connection = fakeConnection
        )
    }

    @Test
    fun `get the details`() {
        val sut = createSUT()

        assertEquals(details, sut.details)
    }

    @Test
    fun `get the connection state`() {
        val sut = createSUT()

        fakeConnection.isConnected.value = true
        assertEquals(true, sut.isConnected.value)

        fakeConnection.isConnected.value = false
        assertEquals(false, sut.isConnected.value)
    }

    @Test
    fun `disconnect from the gateway`() = runTest {
        val sut = createSUT() // connected by default

        sut.disconnect()

        assertEquals(false, sut.isConnected.value)
    }

    @Test
    fun `broadcast a color`() = runTest {
        val sut = createSUT()

        val color = StandardRgbColor(UnitInterval(1.0), UnitInterval(0.5), UnitInterval(0.0))
        sut.broadcastColor(color)

        assertEquals(
            FakeJsonRpcConnection.RecordedNotification("broadcast-color", BroadcastColor(255, 128, 0), timeout = 50.milliseconds),
            fakeConnection.notifications.first()
        )
    }

}