package config

import dsp.filtering.FilterConfig
import dsp.filtering.FilterFamily
import dsp.filtering.FilterOrder
import dsp.windowing.WindowType
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import music.WesternTuningSystem
import serial.SerialFrameFormat
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ConfigFactory(
    private val persistedConfig: PersistedConfig = PersistedConfig()
) {

    fun create(): Config {
        val tuning = WesternTuningSystem()

        return Config(
            startAutomatically = MutableStateFlow(persistedConfig.startAutomatically),
            spectralAnalysis = SpectralAnalysisConfig(
                gainDb = 12f,
                frameDuration = 63.milliseconds,
                approximateBinSpacing = 1f,
                rolloffThreshold = -48f,
                highPassFilter = FilterConfig.HighPass(
                    frequency = tuning.getFrequency(tuning.A, octave = 0),
                    family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
                ),
                lowPassFilter = FilterConfig.LowPass(
                    frequency = tuning.getFrequency(tuning.A, octave = 2),
                    family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
                ),
                window = WindowType.BlackmanHarris3Term,
                peakExtractor = PeakExtractorConfig.Parabolic,
                decimate = true
            ),
            gateway = GatewayConfig(
                autoReconnect = true,
                reconnectInterval = 1.seconds,
                baudRate = 921600,
                frameFormat = SerialFrameFormat.FORMAT_8N1,
            )
        )
    }

}