package lightOrgan.gateway.serial

import jsonrpc.sendRequest
import lightOrgan.gateway.Gateway
import lightOrgan.gateway.GatewayFactory
import lightOrgan.gateway.SerialGatewayDetails
import lightOrgan.gateway.models.GatewayIdentificationResponse
import logging.Logger
import serial.SerialPort
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class SerialGatewayConnector(
    private val connectionFactory: SerialJsonRpcConnectionFactory = SerialJsonRpcConnectionFactory(),
    private val gatewayFactory: GatewayFactory = GatewayFactory(),
    private val timeout: Duration = 10000.milliseconds
) {

    suspend fun connect(port: SerialPort): Gateway {
        val connection = connectionFactory.create(port)
        connection.connect()

        try {
            val response: GatewayIdentificationResponse = connection.sendRequest("gateway-identification-request", null, timeout)

            Logger.success("Port ${port.name} handshake successful.")

            return gatewayFactory.create(
                details = SerialGatewayDetails(
                    macAddress = response.macAddress,
                    firmwareVersion = response.firmwareVersion,
                    baudRate = connection.baudRate,
                    frameFormat = connection.frameFormat
                ),
                connection = connection
            )
        } catch (e: Exception) {
            connection.disconnect()
            throw e
        }
    }

}