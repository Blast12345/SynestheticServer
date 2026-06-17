package lightOrgan.gateway

import annotations.SkipCoverage
import color.rgb.StandardRgbColor
import jsonrpc.JsonRpcConnection
import kotlinx.coroutines.flow.StateFlow
import lightOrgan.gateway.models.BroadcastColor
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

interface Gateway {
    val details: GatewayDetails
    val isConnected: StateFlow<Boolean>

    suspend fun disconnect()
    suspend fun broadcastColor(color: StandardRgbColor)
}

class RealGateway(
    override val details: GatewayDetails,
    private val connection: JsonRpcConnection,
) : Gateway {

    override val isConnected = connection.isConnected


    override suspend fun disconnect() {
        connection.disconnect()
    }

    // ENHANCEMENT: StandardRgbColor could be values of 0-255
    override suspend fun broadcastColor(color: StandardRgbColor) {
        val params = BroadcastColor(
            r = (color.red.value * 255).roundToInt(),
            g = (color.green.value * 255).roundToInt(),
            b = (color.blue.value * 255).roundToInt(),
        )

        connection.sendNotification("broadcast-color", params, 50.milliseconds)
    }

}

@SkipCoverage
class GatewayFactory {
    fun create(details: GatewayDetails, connection: JsonRpcConnection): Gateway = RealGateway(details, connection)
}