package keyBinding

import wrappers.keyBindingSource.JNativeHookKeyEventSource
import wrappers.operatingSystem.Linux
import wrappers.operatingSystem.MacOS
import wrappers.operatingSystem.OperatingSystem
import wrappers.operatingSystem.Windows

interface KeyEventSource {
    fun start(onKeyEvent: (KeyEvent) -> Unit)
    fun stop()
}

// TODO:
class KeyEventSourceProvider(
    val operatingSystem: OperatingSystem = OperatingSystem.current()
) {

    fun create(operatingSystem: OperatingSystem): KeyEventSource {
        return when (operatingSystem) {
            is MacOS -> JNativeHookKeyEventSource()

            is Windows -> JNativeHookKeyEventSource()

            is Linux -> when (operatingSystem.desktop) {
                LinuxDesktop.X11 -> JNativeHookKeyEventSource()
                LinuxDesktop.WAYLAND -> TODO()
                LinuxDesktop.HEADLESS -> TODO()
            }
        }
    }

}