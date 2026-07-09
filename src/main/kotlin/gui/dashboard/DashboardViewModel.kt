package gui.dashboard

import gui.dashboard.tiles.color.ColorTileViewModel
import gui.dashboard.tiles.spectralAnalysis.SpectralAnalysisTileViewModel
import gui.tiles.audioInput.AudioInputTileViewModel
import gui.tiles.gateway.GatewayTileViewModel

class DashboardViewModel(
    val audioInputTileViewModel: AudioInputTileViewModel,
    val spectralAnalysisTileViewModel: SpectralAnalysisTileViewModel,
    val colorTileViewModel: ColorTileViewModel,
    val gatewayTileViewModel: GatewayTileViewModel
)