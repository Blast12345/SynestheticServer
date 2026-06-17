package lightOrgan.gateway

import color.rgb.StandardRgbColor
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import toolkit.monkeyTest.nextGatewayDetails

class FakeGateway : Gateway {

    override val details: GatewayDetails = nextGatewayDetails()
    override val isConnected = MutableStateFlow(true)

    // Disconnect
    private var pauseDisconnect: CompletableDeferred<Unit>? = null
    var disconnectError: Exception? = null

    override suspend fun disconnect() {
        pauseDisconnect?.await()
        disconnectError?.let { throw it }
        isConnected.value = false
    }

    fun pauseDisconnect() {
        pauseDisconnect = CompletableDeferred()
    }

    fun resumeDisconnect() {
        pauseDisconnect?.complete(Unit)
        pauseDisconnect = null
    }

    // Broadcast color
    var lastColor: StandardRgbColor? = null

    override suspend fun broadcastColor(color: StandardRgbColor) {
        lastColor = color
    }

}