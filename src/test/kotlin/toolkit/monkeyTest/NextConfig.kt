package toolkit.monkeyTest

import config.Config
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import kotlin.random.Random

fun nextConfig(): Config {
    return Config(
        startAutomatically = MutableStateFlow(Random.nextBoolean()),
        spectralAnalysis = nextSpectralAnalysisConfig(),
        gateway = GatewayConfig(
            baudRate = nextInt(),
            frameFormat = nextSerialFrameFormat()
        )
    )
}
