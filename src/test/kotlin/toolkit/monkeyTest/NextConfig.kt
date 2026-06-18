package toolkit.monkeyTest

import config.Config
import config.children.Client
import dsp.windowing.WindowType
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalysis.spectrum.SpectrumConfig
import kotlin.random.Random

fun nextConfig(
    clients: Set<Client> = nextClients()
): Config {
    return Config(
        startAutomatically = MutableStateFlow(Random.nextBoolean()),
        clients = clients,
        spectrum = SpectrumConfig(
            gainDb = nextPositiveFloat(),
            frameDuration = nextDuration(),
            approximateBinSpacing = nextPositiveFloat(),
            rolloffThreshold = nextPositiveFloat(),
            highPassFilter = nextHighPassConfig(),
            lowPassFilter = nextLowPassConfig(),
            window = nextEnum<WindowType>()
        ),
        gateway = GatewayConfig(
            baudRate = nextInt(),
            frameFormat = nextSerialFrameFormat()
        )
    )
}