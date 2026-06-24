package hotkeys.core

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import hotkeys.HotkeyProvider

class JNativeHookProvider : HotkeyProvider, NativeKeyListener {

    private val listeners = mutableListOf<(KeyEvent) -> Unit>()

    override fun addListener(listener: (KeyEvent) -> Unit) {
        listeners.add(listener)
    }

    override fun start() {
        GlobalScreen.registerNativeHook()
        GlobalScreen.addNativeKeyListener(this)
    }

    override fun stop() {
        GlobalScreen.removeNativeKeyListener(this)
        GlobalScreen.unregisterNativeHook()
    }

    override fun nativeKeyPressed(event: NativeKeyEvent) {
        val key = mapKey(event.keyCode) ?: return
        val modifiers = mapModifiers(event.modifiers)
        listeners.forEach { it(KeyEvent(key, modifiers)) }
    }

    private fun mapKey(code: Int): Key? = when (code) {
        NativeKeyEvent.VC_PAGE_UP -> Key.PAGE_UP
        NativeKeyEvent.VC_PAGE_DOWN -> Key.PAGE_DOWN
        NativeKeyEvent.VC_HOME -> Key.HOME
        NativeKeyEvent.VC_END -> Key.END
        else -> null
    }

    private fun mapModifiers(mask: Int): Set<Modifier> = buildSet {
        if (mask and NativeKeyEvent.CTRL_L_MASK != 0) add(Modifier.CTRL)
        if (mask and NativeKeyEvent.SHIFT_L_MASK != 0) add(Modifier.SHIFT)
    }
}