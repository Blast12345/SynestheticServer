package config

import gui.dashboard.tiles.spectrum.SpectrumGuiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectrum.SpectrumConfig

@Suppress("LongParameterList")
class Config(
    val startAutomatically: MutableStateFlow<Boolean>,
    val spectrum: SpectrumConfig,
    val spectrumGui: SpectrumGuiConfig,
    val gateway: GatewayConfig
)