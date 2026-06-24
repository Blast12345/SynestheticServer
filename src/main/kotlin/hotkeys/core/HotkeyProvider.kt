package hotkeys

import hotkeys.core.KeyEvent

interface HotkeyProvider {
    fun addListener(listener: (KeyEvent) -> Unit)
    fun start()
    fun stop()
}