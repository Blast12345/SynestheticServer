package hotkeys

import hotkeys.core.JNativeHookProvider
import hotkeys.core.KeyEvent
import hotkeys.core.LinuxInputProvider

interface HotkeyProvider {
    fun addListener(listener: (KeyEvent) -> Unit)
    fun start()
    fun stop()
}

class HotkeyProviderFactory {

    fun create(): HotkeyProvider {
        return if (isLinux()) {
            LinuxInputProvider("/dev/input/eventX")
        } else {
            JNativeHookProvider()
        }
    }

    fun isLinux(): Boolean = System.getProperty("os.name").lowercase().contains("linux")

}