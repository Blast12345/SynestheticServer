package toolkit

import config.AppConfig
import dsp.windowing.WindowType
import lightOrgan.gateway.GatewayConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.conditioning.AudioConditionerConfig
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import serial.SerialFrameFormat
import kotlin.time.Duration.Companion.seconds

val minimalAppConfig = AppConfig(
    spectralAnalysis = SpectralAnalysisConfig(
        audioConditioner = AudioConditionerConfig(
            gainDb = 0f,
            highPassFilter = null,
            lowPassFilter = null,
            rolloffThresholdDb = null,
            decimate = false
        ),
        frameDuration = 1.seconds,
        approximateBinSpacing = 1f,
        window = WindowType.BlackmanHarris3Term,
        peakExtractor = PeakExtractorConfig.Parabolic,
    ),
    gateway = GatewayConfig(
        autoReconnect = false,
        reconnectInterval = 1.seconds,
        baudRate = 921600,
        frameFormat = SerialFrameFormat.FORMAT_8N1,
    )
)