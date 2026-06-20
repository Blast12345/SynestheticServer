package gui.tiles.audioInput

import gui.snackbar.SnackbarController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import lightOrgan.input.AudioInputDetails
import lightOrgan.input.AudioInputManager

class AudioInputTileViewModel(
    private val audioInputManager: AudioInputManager,
    private val snackbarController: SnackbarController,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
) {

    val inputDetails: StateFlow<AudioInputDetails?> = audioInputManager.inputDetails
    val isListening: StateFlow<Boolean> = audioInputManager.isListening

    fun start() {
        scope.launch {
            try {
                audioInputManager.startListening()
            } catch (e: Exception) {
                snackbarController.show(e.message ?: "Failed to connect to input.")
            }
        }
    }

    fun stop() {
        try {
            audioInputManager.stopListening()
        } catch (e: Exception) {
            scope.launch { snackbarController.show(e.message ?: "Failed to disconnect from input.") }
        }
    }

}