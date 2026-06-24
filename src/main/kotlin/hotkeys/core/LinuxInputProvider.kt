package hotkeys.core

import hotkeys.HotkeyProvider
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class LinuxInputProvider(private val devicePath: String) : HotkeyProvider {

    private val listeners = mutableListOf<(KeyEvent) -> Unit>()
    private val activeModifiers = mutableSetOf<Modifier>()

    @Volatile
    private var running = false
    private var thread: Thread? = null

    override fun addListener(listener: (KeyEvent) -> Unit) {
        listeners.add(listener)
    }

    override fun start() {
        running = true
        thread = Thread(::readEvents).apply {
            isDaemon = true
            name = "linux-input-reader"
            start()
        }
    }

    override fun stop() {
        running = false
        thread?.interrupt()
    }

    private fun readEvents() {
        FileInputStream(devicePath).use { stream ->
            val buffer = ByteArray(EVENT_SIZE)
            while (running) {
                if (stream.read(buffer) != EVENT_SIZE) continue
                val parsed = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                val type = parsed.getShort(16).toInt() and 0xFFFF
                val code = parsed.getShort(18).toInt() and 0xFFFF
                val value = parsed.getInt(20)

                if (type == EV_KEY) handleKey(code, value)
            }
        }
    }

    private fun handleKey(code: Int, value: Int) {
        when (code) {
            KEY_LEFTCTRL -> updateModifier(Modifier.CTRL, pressed = value > 0)
            KEY_LEFTSHIFT -> updateModifier(Modifier.SHIFT, pressed = value > 0)
            else -> {
                if (value != 1) return
                val key = mapKey(code) ?: return
                val event = KeyEvent(key, activeModifiers.toSet())
                listeners.forEach { it(event) }
            }
        }
    }

    private fun updateModifier(modifier: Modifier, pressed: Boolean) {
        if (pressed) activeModifiers.add(modifier) else activeModifiers.remove(modifier)
    }

    private fun mapKey(code: Int): Key? = when (code) {
        KEY_PAGEUP -> Key.PAGE_UP
        KEY_PAGEDOWN -> Key.PAGE_DOWN
        KEY_HOME -> Key.HOME
        KEY_END -> Key.END
        else -> null
    }

    companion object {
        private const val EVENT_SIZE = 24
        private const val EV_KEY = 1
        private const val KEY_LEFTCTRL = 29
        private const val KEY_LEFTSHIFT = 42
        private const val KEY_HOME = 102
        private const val KEY_PAGEUP = 104
        private const val KEY_END = 107
        private const val KEY_PAGEDOWN = 109
    }
}