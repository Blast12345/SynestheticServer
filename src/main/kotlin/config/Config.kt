package config

import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalysis.spectrum.SpectralAnalyzerConfig

class Config(
    val startAutomatically: MutableStateFlow<Boolean>,
    val spectralAnalyzer: SpectralAnalyzerConfig,
    val gateway: GatewayConfig
)