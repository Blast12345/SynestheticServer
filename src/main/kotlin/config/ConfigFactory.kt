package config

import dsp.filtering.FilterConfig
import dsp.filtering.FilterFamily
import dsp.filtering.FilterOrder
import dsp.filtering.FilterType
import dsp.windowing.WindowType
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalyzer.spectrum.SpectralAnalyzerConfig
import music.WesternTuningSystem
import serial.SerialFrameFormat
import kotlin.time.Duration.Companion.milliseconds

class ConfigFactory(
    private val persistedConfig: PersistedConfig = PersistedConfig()
) {

    fun create(): Config {
        val tuning = WesternTuningSystem()

        return Config(
            startAutomatically = MutableStateFlow(persistedConfig.startAutomatically),
            spectralAnalyzer = SpectralAnalyzerConfig(
                gainDb = 12f,
                frameDuration = 63.milliseconds,
                approximateBinSpacing = 1f,
                rolloffThreshold = -48f,
                highPassFilter = FilterConfig(
                    type = FilterType.HighPass(tuning.getFrequency(tuning.A, octave = 0)),
                    family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
                ),
                lowPassFilter = FilterConfig(
                    type = FilterType.LowPass(tuning.getFrequency(tuning.A, octave = 2)),
                    family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
                ),
                window = WindowType.BlackmanHarris3Term,
            ),
            gateway = GatewayConfig(
                baudRate = 921600,
                frameFormat = SerialFrameFormat.FORMAT_8N1,
            )
        )
    }

}
