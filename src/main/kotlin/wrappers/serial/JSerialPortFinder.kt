package wrappers.serial

import config.AppConfigSingleton
import lightOrgan.gateway.GatewayConfig
import serial.SerialPort
import serial.SerialPortFinder

class JSerialPortFinder(
    private val config: GatewayConfig = AppConfigSingleton.value.gateway
) : SerialPortFinder {

    override fun find(): List<SerialPort> {


        return com.fazecast.jSerialComm.SerialPort.getCommPorts().map {
            JSerialCommPort(
                port = it,
                config.baudRate,
                config.frameFormat
            )
        }
    }

}
