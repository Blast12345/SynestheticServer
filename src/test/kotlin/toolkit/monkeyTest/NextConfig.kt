package toolkit.monkeyTest

import config.AppConfig

fun nextConfig(): AppConfig {
    return AppConfig(
        spectralAnalysis = nextSpectralAnalysisConfig(),
        gateway = nextGatewayConfig()
    )
}