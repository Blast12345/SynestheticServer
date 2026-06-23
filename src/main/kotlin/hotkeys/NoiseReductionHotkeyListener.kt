package hotkeys

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import config.AppConfigSingleton
import kotlinx.coroutines.flow.update
import logging.Logger

class NoiseReductionHotkeyListener(
    private val step: Double = 0.01
) : NativeKeyListener {

    override fun nativeKeyPressed(event: NativeKeyEvent) {
        val ctrlShift = NativeKeyEvent.CTRL_L_MASK or NativeKeyEvent.SHIFT_L_MASK

        if (event.modifiers and ctrlShift != ctrlShift) return

        when (event.keyCode) {
            NativeKeyEvent.VC_HOME -> adjustGain(step)
            NativeKeyEvent.VC_END -> adjustGain(-step)
        }
    }

    private fun adjustGain(delta: Double) {
        try {
            AppConfigSingleton.update { config ->
                val noiseReduction = config.spectralAnalysis.noiseReduction
                config.copy(
                    spectralAnalysis = config.spectralAnalysis.copy(
                        noiseReduction = noiseReduction.copy(
                            threshold = noiseReduction.threshold + delta
                        )
                    )
                )
            }
        } catch (e: Exception) {
            Logger.error(e)
        }
    }

}