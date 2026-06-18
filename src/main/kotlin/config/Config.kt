package config

import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalysis.spectrum.SpectralAnalysisConfig

class Config(
    val startAutomatically: MutableStateFlow<Boolean>,
    val spectralAnalysis: SpectralAnalysisConfig,
    val gateway: GatewayConfig
)