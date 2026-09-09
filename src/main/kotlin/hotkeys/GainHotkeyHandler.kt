package hotkeys

import config.AppConfigSingleton
import hotkeys.core.Key
import hotkeys.core.KeyEvent
import hotkeys.core.Modifier
import kotlinx.coroutines.flow.update
import logging.Logger

class GainHotkeyHandler(private val stepDb: Float = 1f) {

    fun handle(event: KeyEvent) {
        Logger.warning("Key event received: $event")
        if (!event.hasModifiers(Modifier.CTRL, Modifier.SHIFT)) return

        val delta = when (event.key) {
            Key.PAGE_UP -> stepDb
            Key.PAGE_DOWN -> -stepDb
            else -> return
        }


        AppConfigSingleton.update { config ->
            val conditioner = config.spectralAnalysis.audioConditioner
            val newGain = conditioner.gainDb + delta

            val updatedConfig = config.copy(
                spectralAnalysis = config.spectralAnalysis.copy(
                    audioConditioner = conditioner.copy(
                        gainDb = newGain
                    )
                )
            )

            Logger.success("Set gain to $newGain")

            return@update updatedConfig
        }
    }
}