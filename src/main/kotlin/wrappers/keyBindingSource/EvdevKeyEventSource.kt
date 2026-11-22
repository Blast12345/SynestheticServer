package wrappers.keyBindingSource

import keyBinding.Key
import keyBinding.KeyEvent
import keyBinding.KeyEventSource
import keyBinding.KeyTransition
import kotlinx.coroutines.*
import logging.Logger
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class EvdevKeyEventSource : KeyEventSource {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val openDevices = ConcurrentHashMap<Path, FileInputStream>()
    private val claimedDevices = ConcurrentHashMap.newKeySet<Path>()

    @Volatile
    private var started = false

    override fun start(onKeyEvent: (KeyEvent) -> Unit) {
        check(openDevices.isEmpty()) { "Already started." }

        val watchJob = scope.launch { watchForDevices(onKeyEvent) }
        watchJob.invokeOnCompletion { started = false }
    }

    private suspend fun watchForDevices(onKeyEvent: (KeyEvent) -> Unit) {
        val rescanDelay: Duration = 2.seconds

        while (currentCoroutineContext().isActive) {
            val keyboardPaths = findKeyboardDevices()

            for (keyboardPath in keyboardPaths) {
                if (!claimedDevices.add(keyboardPath)) continue
                scope.launch { readDevice(keyboardPath, onKeyEvent) }
            }

            delay(rescanDelay)
        }
    }

    private fun findKeyboardDevices(): List<Path> {
        val deviceDirectory = Path("/dev/input/by-id")
        val KEYBOARD_SUFFIX = "-event-kbd"

        return runCatching {
            Files.list(deviceDirectory).use { paths ->
                paths.filter { it.name.endsWith(KEYBOARD_SUFFIX) }
                    .map { it.toRealPath() }
                    .distinct()
                    .toList()
            }
        }.getOrDefault(emptyList())
    }

    private fun readDevice(devicePath: Path, onKeyEvent: (KeyEvent) -> Unit) {
        try {
            FileInputStream(devicePath.toFile()).use { input ->
                openDevices[devicePath] = input
                readEvents(input, onKeyEvent)
            }
        } catch (exception: IOException) {
            Logger.error("Stopped reading $devicePath", exception)
        } finally {
            openDevices.remove(devicePath)
            claimedDevices.remove(devicePath)
        }
    }

    private val EVENT_LAYOUT = InputEventLayout.forCurrentPlatform()
    private val EV_KEY = 1 // Keys and buttons? Perhaps make this an enum?

    private fun readEvents(input: FileInputStream, onKeyEvent: (KeyEvent) -> Unit) {
        val buffer = ByteArray(EVENT_LAYOUT.size)

        while (true) {
            if (input.readNBytes(buffer, 0, EVENT_LAYOUT.size) != EVENT_LAYOUT.size) {
                return
            }

            val keyEvent = parseKeyEvent(buffer)
            if (keyEvent != null) {
                onKeyEvent(keyEvent)
            }
        }
    }

    private fun parseKeyEvent(buffer: ByteArray): KeyEvent? {
        val parsed = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)

        if (parsed.getShort(EVENT_LAYOUT.typeOffset).toInt() != EV_KEY) {
            return null
        }

        val evdevCode = parsed.getShort(EVENT_LAYOUT.codeOffset).toInt()
        val key = keysByEvdevCode[evdevCode] ?: return null

        val transition = when (parsed.getInt(EVENT_LAYOUT.valueOffset)) {
            EVDEV_KEY_RELEASED -> KeyTransition.RELEASED
            EVDEV_KEY_PRESSED, EVDEV_KEY_REPEATED -> KeyTransition.PRESSED
            else -> return null
        }

        return KeyEvent(key, transition)
    }

    private val EVDEV_KEY_RELEASED = 0
    private val EVDEV_KEY_PRESSED = 1
    private val EVDEV_KEY_REPEATED = 2

    private val keysByEvdevCode: Map<Int, Key> = Key.entries
        .flatMap { key -> key.toEvdevCodes().map { code -> code to key } }
        .toMap()

    private fun Key.toEvdevCodes(): Set<Int> = when (this) {
        // Modifiers
        Key.CONTROL -> setOf(KEY_LEFTCTRL, KEY_RIGHTCTRL)
        Key.SHIFT -> setOf(KEY_LEFTSHIFT, KEY_RIGHTSHIFT)

        // Arrow keys
        Key.UP -> setOf(KEY_UP)
        Key.DOWN -> setOf(KEY_DOWN)
        Key.LEFT -> setOf(KEY_LEFT)
        Key.RIGHT -> setOf(KEY_RIGHT)
    }

    companion object {
        private const val EV_KEY = 1
        private const val EVDEV_KEY_RELEASED = 0
        private const val EVDEV_KEY_PRESSED = 1
        private const val EVDEV_KEY_REPEATED = 2

        private const val KEY_LEFTCTRL = 29
        private const val KEY_RIGHTCTRL = 97
        private const val KEY_LEFTSHIFT = 42
        private const val KEY_RIGHTSHIFT = 54
        private const val KEY_UP = 103
        private const val KEY_LEFT = 105
        private const val KEY_RIGHT = 106
        private const val KEY_DOWN = 108
    }

    override fun stop() {
        scope.coroutineContext.cancelChildren()

        openDevices.values.forEach { device -> runCatching { device.close() } }
        openDevices.clear()
        claimedDevices.clear()
    }

}


private enum class InputEventLayout(
    val size: Int,
    val typeOffset: Int,
    val codeOffset: Int,
    val valueOffset: Int,
) {
    /** `timeval` is two 32-bit longs. */
    BITS_32(size = 16, typeOffset = 8, codeOffset = 10, valueOffset = 12),

    /** `timeval` is two 64-bit longs. */
    BITS_64(size = 24, typeOffset = 16, codeOffset = 18, valueOffset = 20);

    companion object {
        fun forCurrentPlatform(): InputEventLayout {
            return when (System.getProperty("sun.arch.data.model")) {
                "32" -> BITS_32
                else -> BITS_64
            }
        }
    }
}