package lightOrgan.gateway

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import lightOrgan.gateway.GatewayManager.Event
import lightOrgan.gateway.GatewayManager.State
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import toolkit.extensions.collectInto
import toolkit.monkeyTest.nextException
import toolkit.monkeyTest.nextGatewayConfig

@OptIn(ExperimentalCoroutinesApi::class)
class GatewayManagerTests {

    private val config = nextGatewayConfig()
    private lateinit var fakeGatewayFinder: FakeGatewayFinder

    @BeforeEach
    fun setupHappyPath() {
        fakeGatewayFinder = FakeGatewayFinder()
    }

    private fun createSUT(scope: CoroutineScope, config: GatewayConfig = this.config): RealGatewayManager {
        return RealGatewayManager(
            config = config,
            gatewayFinder = fakeGatewayFinder,
            scope = scope
        )
    }

    // Connect
    @Test
    fun `connect to a gateway`() = runTest {
        val sut = createSUT(backgroundScope)

        // Start connection attempt
        fakeGatewayFinder.pause()
        launch { sut.connect() }
        runCurrent()

        assertEquals(State.Connecting, sut.state.value)

        // Finish connecting
        fakeGatewayFinder.resume()
        runCurrent()

        assertEquals(State.Connected(fakeGatewayFinder.gateway), sut.state.value)
    }

    @Test
    fun `given the manager is connecting, then calling connect again throws an error`() = runTest {
        val sut = createSUT(backgroundScope)

        // Start first connection attempt
        fakeGatewayFinder.pause()
        launch { sut.connect() }
        runCurrent()

        // Start second connection attempt
        assertThrows<Exception> { sut.connect() }
        runCurrent()

        // Finish first attempt
        fakeGatewayFinder.resume()
        runCurrent()

        assertEquals(State.Connected(fakeGatewayFinder.gateway), sut.state.value)
    }

    @Test
    fun `given the manager is connected, then calling connect again throws an error`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.connect()

        assertThrows<Exception> { sut.connect() }
        assertEquals(State.Connected(fakeGatewayFinder.gateway), sut.state.value)
    }

    @Test
    fun `given the manager is disconnecting, then calling connect throws an error`() = runTest {
        val sut = createSUT(backgroundScope)

        // Start connection attempt and hang on disconnect
        sut.connect()
        fakeGatewayFinder.gateway.pauseDisconnect()
        launch { sut.disconnect() }
        runCurrent()

        assertEquals(State.Disconnecting, sut.state.value)

        // Start second connection attempt
        assertThrows<Exception> { sut.connect() }

        // Finish disconnecting
        fakeGatewayFinder.gateway.resumeDisconnect()
        runCurrent()

        assertEquals(State.Disconnected, sut.state.value)
    }

    @Test
    fun `when the gateway finder throws, then throw an error and remain connected`() = runTest {
        val sut = createSUT(backgroundScope)
        fakeGatewayFinder.error = nextException()

        assertThrows<Exception> { sut.connect() }
    }

    // Disconnect
    @Test
    fun `disconnect from a gateway`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.connect()

        // Start disconnect attempt
        fakeGatewayFinder.gateway.pauseDisconnect()
        launch { sut.disconnect() }
        runCurrent()

        assertEquals(State.Disconnecting, sut.state.value)

        // Finish disconnecting
        fakeGatewayFinder.gateway.resumeDisconnect()
        runCurrent()

        assertEquals(State.Disconnected, sut.state.value)
    }

    @Test
    fun `given the manager is disconnected, then calling disconnect again throws an error`() = runTest {
        val sut = createSUT(backgroundScope)

        assertThrows<Exception> { sut.disconnect() }
    }

    @Test
    fun `given the manager is connecting, then calling disconnect again throws an error`() = runTest {
        val sut = createSUT(backgroundScope)

        // Start connection attempt
        fakeGatewayFinder.pause()
        launch { sut.connect() }
        runCurrent()

        // Attempt disconnect
        assertThrows<Exception> { sut.disconnect() }
        runCurrent()

        // Finish connection attempt
        fakeGatewayFinder.resume()
        runCurrent()

        assertEquals(State.Connected(fakeGatewayFinder.gateway), sut.state.value)
    }

    @Test
    fun `given the manager is disconnecting, then calling disconnect again throws an error`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.connect()

        // Start first disconnect attempt
        fakeGatewayFinder.gateway.pauseDisconnect()
        launch { sut.disconnect() }
        runCurrent()

        // Start second disconnect attempt
        assertThrows<Exception> { sut.disconnect() }

        // Finish first disconnect attempt
        fakeGatewayFinder.gateway.resumeDisconnect()
        runCurrent()

        assertEquals(State.Disconnected, sut.state.value)
    }

    @Test
    fun `when the gateway fails to disconnect, then throw an error and remain connected`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.connect()
        fakeGatewayFinder.gateway.disconnectError = nextException()

        assertThrows<Exception> { sut.disconnect() }
        assertEquals(State.Connected(fakeGatewayFinder.gateway), sut.state.value)
    }

    // Unexpected disconnects
    @Test
    fun `when the gateway unexpectedly disconnects, then emit an event`() = runTest {
        val sut = createSUT(backgroundScope)
        val events = sut.events.collectInto(this)
        sut.connect()

        // Force a disconnect
        fakeGatewayFinder.gateway.isConnected.value = false
        runCurrent()

        assertEquals(State.Disconnected, sut.state.value)
        assertEquals(Event.UnexpectedDisconnect, events.first())
    }

    // Auto-reconnect
    @Test
    fun `given auto-reconnect is enabled, when the gateway unexpectedly disconnects, then reconnect`() = runTest {
        val sut = createSUT(backgroundScope, config.copy(autoReconnect = true))
        sut.connect()

        fakeGatewayFinder.gateway.isConnected.value = false
        fakeGatewayFinder.gateway = FakeGateway() // new gateway
        runCurrent()

        assertEquals(State.Connected(fakeGatewayFinder.gateway), sut.state.value)
    }

    @Test
    fun `when auto-reconnect is disabled and the gateway unexpectedly disconnects, then stay disconnected`() = runTest {
        val sut = createSUT(backgroundScope, config.copy(autoReconnect = false))
        sut.connect()

        fakeGatewayFinder.gateway.isConnected.value = false
        runCurrent()

        assertEquals(State.Disconnected, sut.state.value)
    }

    @Test
    fun `when auto-reconnect fails, then retry after the interval`() = runTest {
        val sut = createSUT(backgroundScope, config.copy(autoReconnect = true))
        sut.connect()

        // Disconnect and return error on next find
        fakeGatewayFinder.gateway.isConnected.value = false
        fakeGatewayFinder.error = nextException()
        runCurrent()

        assertEquals(State.Disconnected, sut.state.value)

        // Fix the error and find a new gateway
        fakeGatewayFinder.error = null
        fakeGatewayFinder.gateway = FakeGateway()
        testScheduler.advanceTimeBy(config.reconnectInterval)
        runCurrent()

        assertEquals(State.Connected(fakeGatewayFinder.gateway), sut.state.value)
    }

}