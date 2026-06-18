package config

import config.children.Client
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalysis.spectrum.SpectrumConfig

@Suppress("LongParameterList")
class Config(
    val startAutomatically: MutableStateFlow<Boolean>,
    val clients: Set<Client>,
    val spectrum: SpectrumConfig,
    val gateway: GatewayConfig
)