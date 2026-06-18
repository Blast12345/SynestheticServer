package gui.dashboard

import config.Config
import config.ConfigPersister
import config.ConfigSingleton
import gui.dashboard.tiles.color.ColorTileViewModel
import gui.dashboard.tiles.spectralAnalyzer.SpectralAnalysisTileViewModel
import gui.snackbar.SnackbarController
import gui.tiles.audioInput.AudioInputTileViewModel
import gui.tiles.gateway.GatewayTileViewModel
import lightOrgan.color.ColorManager
import lightOrgan.gateway.GatewayManager
import lightOrgan.input.AudioInputManager
import lightOrgan.spectralAnalysis.SpectralAnalyzer

class DashboardViewModel(
    inputManager: AudioInputManager,
    spectralAnalyzer: SpectralAnalyzer,
    colorManager: ColorManager,
    gatewayManager: GatewayManager,
    snackbarController: SnackbarController,
    private val configPersister: ConfigPersister = ConfigPersister(),
    private val config: Config = ConfigSingleton
) {

    val audioInputTileViewModel = AudioInputTileViewModel(inputManager, snackbarController)
    val spectralAnalysisTileViewModel = SpectralAnalysisTileViewModel(spectralAnalyzer)
    val colorTileViewModel = ColorTileViewModel(colorManager)
    val gatewayTileViewModel = GatewayTileViewModel(gatewayManager, snackbarController)

    init {
        startPersistingConfigChanges()
    }

    private fun startPersistingConfigChanges() {
        configPersister.persist(config)
    }

}