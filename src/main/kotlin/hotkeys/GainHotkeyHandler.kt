package hotkeys

import config.AppConfigSingleton
import hotkeys.core.Key
import hotkeys.core.KeyEvent
import hotkeys.core.Modifier
import kotlinx.coroutines.flow.update

class GainHotkeyHandler(private val stepDb: Float = 1f) {

    fun handle(event: KeyEvent) {
        if (!event.hasModifiers(Modifier.CTRL, Modifier.SHIFT)) return

        val delta = when (event.key) {
            Key.PAGE_UP -> stepDb
            Key.PAGE_DOWN -> -stepDb
            else -> return
        }

        AppConfigSingleton.update { config ->
            val conditioner = config.spectralAnalysis.audioConditioner
            config.copy(
                spectralAnalysis = config.spectralAnalysis.copy(
                    audioConditioner = conditioner.copy(
                        gainDb = conditioner.gainDb + delta
                    )
                )
            )
        }
    }

}