package controls

import keyBinding.*
import logging.Logger

// ServerCommandService? ServerCommandManager? ServerCommandHandler?
class ServerCommandListener(
    private val keyEventSource: KeyEventSource,
    private val keymap: Keymap<ServerCommand> = DefaultKeymap,
    private val executor: ServerCommandExecutor = ServerCommandExecutor(),
) {

    companion object {
        fun default(): ServerCommandListener? {
            val keyEventSource = KeyEventSource.default() ?: return null
            return ServerCommandListener(keyEventSource)
        }
    }

    private val heldKeys = mutableSetOf<Key>()

    fun start() {
        keyEventSource.start(::handleKeyEvent)
    }

    // TODO: Try/catch safety
    private fun handleKeyEvent(event: KeyEvent) {
        when (event.transition) {
            KeyTransition.PRESSED -> heldKeys.add(event.key)
            KeyTransition.RELEASED -> heldKeys.remove(event.key)
        }

        if (event.transition == KeyTransition.PRESSED) {
            val command = keymap.commandFor(heldKeys) ?: return
            runCatching { executor.execute(command) }.onFailure { Logger.error(it) }
        }
    }

    fun stop() {
        keyEventSource.stop()
        heldKeys.clear()
    }

}