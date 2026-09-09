package keyBinding

import wrappers.keyBindingSource.EvdevKeyEventSource
import wrappers.keyBindingSource.JNativeHookKeyEventSource
import wrappers.operatingSystem.Linux
import wrappers.operatingSystem.MacOS
import wrappers.operatingSystem.OperatingSystem
import wrappers.operatingSystem.Windows

interface KeyEventSource {
    fun start(onKeyEvent: (KeyEvent) -> Unit)
    fun stop()

    companion object {
        fun default(): KeyEventSource? {
            val currentOperatingSystem = OperatingSystem.current()

            return when (currentOperatingSystem) {
                is MacOS -> JNativeHookKeyEventSource()
                is Windows -> JNativeHookKeyEventSource()
                is Linux -> EvdevKeyEventSource()
                null -> null
            }
        }
    }
}