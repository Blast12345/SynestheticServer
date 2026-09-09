package wrappers.operatingSystem

import logging.Logger

sealed interface OperatingSystem {
    companion object {
        fun current(): OperatingSystem? {
            val osName = System.getProperty("os.name").orEmpty()
            val operatingSystem = fromName(osName)

            if (operatingSystem == null) {
                Logger.error("Unrecognized operating system: '$osName'")
            }

            return operatingSystem
        }

        fun fromName(name: String): OperatingSystem? {
            return when {
                name.startsWith("mac", ignoreCase = true) -> MacOS
                name.startsWith("win", ignoreCase = true) -> Windows
                name.contains("linux", ignoreCase = true) -> Linux
                else -> null
            }
        }
    }
}

object MacOS : OperatingSystem
object Windows : OperatingSystem
object Linux : OperatingSystem