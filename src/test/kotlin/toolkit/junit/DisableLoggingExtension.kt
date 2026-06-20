package toolkit.junit

import logging.LogLevel
import logging.Logger
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext

class DisableLoggingExtension : BeforeAllCallback {
    override fun beforeAll(context: ExtensionContext) {
        Logger.level = LogLevel.OFF
    }
}