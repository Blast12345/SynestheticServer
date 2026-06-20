package lightOrgan.gateway

import config.ConfigSingleton
import extensions.await
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import lightOrgan.gateway.GatewayManager.Event
import lightOrgan.gateway.GatewayManager.State
import lightOrgan.gateway.serial.SerialGatewayFinder

interface GatewayManager {
    val state: StateFlow<State>
    val events: SharedFlow<Event>

    suspend fun connect()
    suspend fun disconnect()

    // Types
    sealed interface State {
        data object Disconnected : State
        data object Connecting : State
        data class Connected(val gateway: Gateway) : State
        data object Disconnecting : State
    }

    sealed interface Event {
        data object UnexpectedDisconnect : Event
    }
}

// ENHANCEMENT: Auto-reconnect
@OptIn(ExperimentalCoroutinesApi::class)
class RealGatewayManager(
    private val config: GatewayConfig = ConfigSingleton.gateway,
    private val gatewayFinder: GatewayFinder = SerialGatewayFinder(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
) : GatewayManager {


    private val _state = MutableStateFlow<State>(State.Disconnected)
    override val state: StateFlow<State> = _state

    private val _events = MutableSharedFlow<Event>()
    override val events: SharedFlow<Event> = _events.asSharedFlow()

    private var unexpectedDisconnectWatcher: Job? = null

    override suspend fun connect() {
        when (_state.value) {
            is State.Disconnected -> _state.value = State.Connecting
            is State.Connecting -> throw IllegalStateException("Could not connect: there is a connection attempt in progress.")
            is State.Connected -> throw IllegalStateException("Could not connect: there is already a gateway connected.")
            is State.Disconnecting -> throw IllegalStateException("Could not connect: there is a disconnect attempt in progress.")
        }

        try {
            val gateway = gatewayFinder.find()
            _state.value = State.Connected(gateway)
            unexpectedDisconnectWatcher = watchForUnexpectedDisconnect(gateway)
        } catch (e: Exception) {
            _state.value = State.Disconnected
            throw e
        }
    }

    private fun watchForUnexpectedDisconnect(gateway: Gateway) = scope.launch {
        gateway.isConnected.await(false)
        _state.value = State.Disconnected
        _events.emit(Event.UnexpectedDisconnect)

        if (config.autoReconnect) {
            reconnect()
        }
    }

    private suspend fun reconnect() {
        while (true) {
            try {
                connect()
                return
            } catch (_: Exception) {
                delay(config.reconnectInterval)
            }
        }
    }

    override suspend fun disconnect() {
        val gateway = when (val state = _state.value) {
            is State.Disconnected -> throw IllegalStateException("Could not disconnect: there is no connection.")
            is State.Connecting -> throw IllegalStateException("Could not disconnect: there is a connection attempt in progress.")
            is State.Connected -> state.gateway
            is State.Disconnecting -> throw IllegalStateException("Could not connect: there is a disconnect attempt in progress.")
        }

        _state.value = State.Disconnecting

        try {
            unexpectedDisconnectWatcher?.cancel()
            gateway.disconnect()
            _state.value = State.Disconnected
        } catch (e: Exception) {
            _state.value = State.Connected(gateway)
            unexpectedDisconnectWatcher = watchForUnexpectedDisconnect(gateway)
            throw e
        }
    }

}