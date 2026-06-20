package gui.dashboard

import gui.dashboard.tiles.color.ColorTileViewModel
import gui.dashboard.tiles.spectralAnalysis.SpectralAnalysisTileViewModel
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
    snackbarController: SnackbarController
) {

    val audioInputTileViewModel = AudioInputTileViewModel(inputManager, snackbarController)
    val spectralAnalysisTileViewModel = SpectralAnalysisTileViewModel(spectralAnalyzer)
    val colorTileViewModel = ColorTileViewModel(colorManager)
    val gatewayTileViewModel = GatewayTileViewModel(gatewayManager, snackbarController)

}