package toolkit.monkeyTest

import config.AppConfig

fun nextAppConfig(): AppConfig {
    return AppConfig(
        spectralAnalysis = nextSpectralAnalysisConfig(),
        gateway = nextGatewayConfig()
    )
}