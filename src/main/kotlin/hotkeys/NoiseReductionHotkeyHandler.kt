package hotkeys

import config.AppConfigSingleton
import hotkeys.core.Key
import hotkeys.core.KeyEvent
import hotkeys.core.Modifier
import kotlinx.coroutines.flow.update

class NoiseReductionHotkeyHandler(private val step: Double = 0.01) {

    fun handle(event: KeyEvent) {
        if (!event.hasModifiers(Modifier.CTRL, Modifier.SHIFT)) return

        val delta = when (event.key) {
            Key.HOME -> step
            Key.END -> -step
            else -> return
        }

        AppConfigSingleton.update { config ->
            val noiseReduction = config.spectralAnalysis.noiseReduction
            val newThreshold = (noiseReduction.threshold + delta).coerceIn(0.0, 0.99)

            config.copy(
                spectralAnalysis = config.spectralAnalysis.copy(
                    noiseReduction = noiseReduction.copy(
                        threshold = newThreshold
                    )
                )
            )
        }
    }
}