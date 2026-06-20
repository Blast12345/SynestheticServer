package toolkit.monkeyTest

import config.Config
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.random.Random

fun nextConfig(): Config {
    return Config(
        startAutomatically = MutableStateFlow(Random.nextBoolean()),
        spectralAnalysis = nextSpectralAnalysisConfig(),
        gateway = nextGatewayConfig()
    )
}