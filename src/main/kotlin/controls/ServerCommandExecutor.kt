package controls

import config.AppConfigSingleton
import kotlinx.coroutines.flow.update
import lightOrgan.spectralAnalysis.noiseReduction.SpectralGate

class ServerCommandExecutor {

    fun execute(command: ServerCommand) = when (command) {
        ServerCommand.GAIN_UP -> increaseGain()
        ServerCommand.GAIN_DOWN -> decreaseGain()
        ServerCommand.INCREASE_NOISE_FLOOR -> increaseNoiseFloor()
        ServerCommand.DECREASE_NOISE_FLOOR -> decreaseNoiseFloor()
    }

    // Gain
    private fun increaseGain() {
        adjustGain(1f)
    }

    private fun decreaseGain() {
        adjustGain(-1f)
    }

    private fun adjustGain(stepSize: Float) {
        AppConfigSingleton.update { config ->
            val audioConditioner = config.spectralAnalysis.audioConditioner

            config.copy(
                spectralAnalysis = config.spectralAnalysis.copy(
                    audioConditioner = audioConditioner.copy(
                        gainDb = audioConditioner.gainDb + stepSize
                    )
                )
            )
        }
    }

    // Noise floor
    private fun increaseNoiseFloor() {
        adjustNoiseFloor(1.0)
    }

    private fun decreaseNoiseFloor() {
        adjustNoiseFloor(-1.0)
    }

    private fun adjustNoiseFloor(stepSize: Double) {
        AppConfigSingleton.update { config ->
            val spectralAnalysis = config.spectralAnalysis
            val postProcessor = spectralAnalysis.postProcessor
            val noiseReducer = postProcessor.noiseReducer as SpectralGate.Config

            val newThreshold = noiseReducer.thresholdDb + stepSize

            // TODO: The rescaling assumes the maximum value would be a magnitude of 1 / dbFS of 0
            if (newThreshold >= 0.0) {
                return
            }

            config.copy(
                spectralAnalysis = config.spectralAnalysis.copy(
                    postProcessor = postProcessor.copy(
                        noiseReducer = noiseReducer.copy(thresholdDb = newThreshold)
                    )
                )
            )
        }
    }

}