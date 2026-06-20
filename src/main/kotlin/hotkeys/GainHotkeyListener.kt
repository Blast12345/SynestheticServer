package hotkeys

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import config.AppConfigSingleton
import kotlinx.coroutines.flow.update

class GainHotkeyListener(
    private val stepDb: Float = 3f
) : NativeKeyListener {

    override fun nativeKeyPressed(event: NativeKeyEvent) {
        val ctrlShift = NativeKeyEvent.CTRL_L_MASK or NativeKeyEvent.SHIFT_L_MASK

        if (event.modifiers and ctrlShift != ctrlShift) return

        when (event.keyCode) {
            NativeKeyEvent.VC_UP -> adjustGain(stepDb)
            NativeKeyEvent.VC_DOWN -> adjustGain(-stepDb)
        }
    }

    private fun adjustGain(delta: Float) {
        AppConfigSingleton.update { config ->
            val audioConditioner = config.spectralAnalysis.audioConditioner
            config.copy(
                spectralAnalysis = config.spectralAnalysis.copy(
                    audioConditioner = audioConditioner.copy(
                        gainDb = audioConditioner.gainDb + delta
                    )
                )
            )
        }
    }

}