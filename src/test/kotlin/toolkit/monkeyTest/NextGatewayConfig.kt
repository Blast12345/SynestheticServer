package toolkit.monkeyTest

import lightOrgan.gateway.GatewayConfig
import kotlin.random.Random

fun nextGatewayConfig(): GatewayConfig {
    return GatewayConfig(
        autoReconnect = Random.nextBoolean(),
        reconnectInterval = nextDuration(),
        baudRate = nextInt(),
        frameFormat = nextSerialFrameFormat()
    )
}
