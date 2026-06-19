package toolkit.monkeyTest

import config.Config
import dsp.windowing.WindowType
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalysis.spectrum.SpectralAnalyzerConfig
import kotlin.random.Random

fun nextConfig(): Config {
    return Config(
        startAutomatically = MutableStateFlow(Random.nextBoolean()),
        spectralAnalyzer = SpectralAnalyzerConfig(
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