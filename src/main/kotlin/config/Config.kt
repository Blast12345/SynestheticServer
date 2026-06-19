package config

import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalyzer.spectrum.SpectralAnalyzerConfig

class Config(
    val startAutomatically: MutableStateFlow<Boolean>,
    val spectralAnalyzer: SpectralAnalyzerConfig,
    val gateway: GatewayConfig
)