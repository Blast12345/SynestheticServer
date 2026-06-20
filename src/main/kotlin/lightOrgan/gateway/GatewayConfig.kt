package lightOrgan.gateway

import serial.SerialFrameFormat
import kotlin.time.Duration

data class GatewayConfig(
    val autoReconnect: Boolean,
    val reconnectInterval: Duration,
    val baudRate: Int,
    val frameFormat: SerialFrameFormat
)