package config

import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig

class AppConfig(
    val spectralAnalysis: SpectralAnalysisConfig,
    val gateway: GatewayConfig
)