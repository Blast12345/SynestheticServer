package toolkit.monkeyTest

import config.Config
import dsp.windowing.WindowType
import gui.dashboard.tiles.spectrum.SpectrumGuiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectrum.SpectrumConfig
import kotlin.random.Random

fun nextConfig(): Config {
    return Config(
        startAutomatically = MutableStateFlow(Random.nextBoolean()),
        spectrum = SpectrumConfig(
            gainDb = nextPositiveFloat(),
            frameDuration = nextDuration(),
            approximateBinSpacing = nextPositiveFloat(),
            rolloffThreshold = nextPositiveFloat(),
            highPassFilter = nextHighPassConfig(),
            lowPassFilter = nextLowPassConfig(),
            window = nextEnum<WindowType>()
        ),
        spectrumGui = SpectrumGuiConfig(
            lowestFrequency = Random.nextFloat(),
            highestFrequency = Random.nextFloat(),
        ),
        gateway = GatewayConfig(
            baudRate = nextInt(),
            frameFormat = nextSerialFrameFormat()
        )
    )
}