package hotkeys.core

import hotkeys.HotkeyProvider
import kotlinx.coroutines.*
import logging.Logger
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.concurrent.CopyOnWriteArrayList

class LinuxInputProvider(
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob()),
    private val deviceName: String = "SayoDevice",
    private val reconnectDelayMs: Long = 2000,
) : HotkeyProvider {

    private val listeners = CopyOnWriteArrayList<(KeyEvent) -> Unit>()
    private val activeModifiers = mutableSetOf<Modifier>()

    private var stream: FileInputStream? = null
    private var job: Job? = null

    override fun addListener(listener: (KeyEvent) -> Unit) {
        listeners.add(listener)
    }

    override fun start() {
        job = scope.launch(Dispatchers.IO) {
            connectionLoop()
        }
    }

    override fun stop() {
        stream?.close()
        job?.cancel()
    }

    private suspend fun connectionLoop() {
        while (true) {
            val devicePath = findDevice()
            if (devicePath == null) {
                delay(reconnectDelayMs)
                continue
            }

            readEvents(devicePath)
            activeModifiers.clear()
            delay(reconnectDelayMs)
        }
    }

    private fun readEvents(devicePath: String) {
        try {
            FileInputStream(devicePath).use { input ->
                stream = input
                val buffer = ByteArray(EVENT_SIZE)

                while (true) {
                    val bytesRead = input.read(buffer)
                    if (bytesRead == -1) return
                    if (bytesRead != EVENT_SIZE) continue

                    val parsed = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)
                    val type = parsed.getShort(16).toInt() and 0xFFFF
                    val code = parsed.getShort(18).toInt() and 0xFFFF
                    val value = parsed.getInt(20)

                    if (type == EV_KEY) handleKey(code, value)
                }
            }
        } catch (e: IOException) {
            Logger.error(e)
            // Device disconnected or stream closed during shutdown
        }
    }

    private fun findDevice(): String? {
        val byId = File("/dev/input/by-id/")
        val device = byId.listFiles()
            ?.firstOrNull { it.name.contains(deviceName) && it.name.endsWith("-event-kbd") }
        return device?.canonicalPath
    }

    private fun handleKey(code: Int, value: Int) {
        val modifier = toModifier(code)
        if (modifier != null) {
            if (value > 0) activeModifiers.add(modifier) else activeModifiers.remove(modifier)
            return
        }

        if (value != 1 && value != 2) return
        val key = toKey(code) ?: return
        val event = KeyEvent(key, activeModifiers.toSet())
        listeners.forEach { it(event) }
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

        private fun toModifier(code: Int): Modifier? = when (code) {
            KEY_LEFTCTRL -> Modifier.CTRL
            KEY_LEFTSHIFT -> Modifier.SHIFT
            else -> null
        }

        private fun toKey(code: Int): Key? = when (code) {
            KEY_PAGEUP -> Key.PAGE_UP
            KEY_PAGEDOWN -> Key.PAGE_DOWN
            KEY_HOME -> Key.HOME
            KEY_END -> Key.END
            else -> null
        }
    }
}