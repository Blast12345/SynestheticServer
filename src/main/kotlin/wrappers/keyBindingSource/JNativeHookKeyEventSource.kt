package wrappers.keyBindingSource

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import keyBinding.Key
import keyBinding.KeyEvent
import keyBinding.KeyEventSource
import keyBinding.KeyTransition

class JNativeHookKeyEventSource : KeyEventSource {

    private var listener: NativeKeyListener? = null

    override fun start(onKeyEvent: (KeyEvent) -> Unit) {
        check(listener == null) { "A listener is already active." }

        GlobalScreen.registerNativeHook()

        val newListener = KeyEventTranslatingListener(onKeyEvent)
        GlobalScreen.addNativeKeyListener(newListener)

        listener = newListener
    }

    override fun stop() {
        listener?.let { GlobalScreen.removeNativeKeyListener(it) }
        listener = null

        if (GlobalScreen.isNativeHookRegistered()) {
            GlobalScreen.unregisterNativeHook() // TODO: Global reference counter?
        }
    }

}

private class KeyEventTranslatingListener(
    private val onKeyEvent: (KeyEvent) -> Unit,
) : NativeKeyListener {

    override fun nativeKeyPressed(nativeEvent: NativeKeyEvent) =
        translate(nativeEvent, KeyTransition.PRESSED)

    override fun nativeKeyReleased(nativeEvent: NativeKeyEvent) =
        translate(nativeEvent, KeyTransition.RELEASED)

    private fun translate(nativeEvent: NativeKeyEvent, transition: KeyTransition) {
        val key = keysByNativeKeyCode[nativeEvent.keyCode] ?: return
        onKeyEvent(KeyEvent(key, transition))
    }

    private val keysByNativeKeyCode: Map<Int, Key> = Key.entries.associateBy { it.toNativeKeyCode() }

    private fun Key.toNativeKeyCode(): Int = when (this) {
        // Modifiers
        Key.CONTROL -> NativeKeyEvent.VC_CONTROL
        Key.SHIFT -> NativeKeyEvent.VC_SHIFT

        // Arrow keys
        Key.UP -> NativeKeyEvent.VC_UP
        Key.DOWN -> NativeKeyEvent.VC_DOWN
        Key.LEFT -> NativeKeyEvent.VC_LEFT
        Key.RIGHT -> NativeKeyEvent.VC_RIGHT
    }

}