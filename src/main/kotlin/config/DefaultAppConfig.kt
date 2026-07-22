package config

import dsp.filtering.FilterConfig
import dsp.filtering.FilterFamily
import dsp.filtering.FilterOrder
import dsp.windowing.WindowType
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.conditioning.AudioConditionerConfig
import lightOrgan.spectralAnalysis.conditioning.DecimationConfig
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculatorConfig
import music.WesternTuningSystem
import serial.SerialFrameFormat
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

private val tuning = WesternTuningSystem()

val DefaultAppConfig = AppConfig(
    spectralAnalysis = SpectralAnalysisConfig(
        audioConditioner = AudioConditionerConfig(
            gainDb = 12f,
            highPassFilter = FilterConfig.HighPass(
                frequency = tuning.getFrequency(tuning.A, octave = 0),
                family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
            ),
            lowPassFilter = FilterConfig.LowPass(
                frequency = tuning.getFrequency(tuning.A, octave = 2),
                family = FilterFamily.Butterworth(FilterOrder.fromDbPerOctave(48)),
            ),
            rolloffThresholdDb = -48f,
            decimation = DecimationConfig.Automatic(-48f)
        ),
        spectrumCalculator = SpectrumCalculatorConfig(
            window = WindowType.BlackmanHarris3Term,
            frameDuration = 63.milliseconds,
            approximateBinSpacing = 1f,
        ),
        peakExtractor = PeakExtractorConfig.Parabolic,
    ),
    gateway = GatewayConfig(
        autoReconnect = true,
        reconnectInterval = 1.seconds,
        baudRate = 921600,
        frameFormat = SerialFrameFormat.FORMAT_8N1,
    )
)