package lightOrgan

import color.StandardRgbColor
import config.AppConfig
import config.AppConfigSingleton
import dsp.windowing.WindowType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import lightOrgan.color.ColorCalculator
import lightOrgan.gateway.FakeGatewayManager
import lightOrgan.gateway.GatewayConfig
import lightOrgan.input.FakeAudioInputManager
import lightOrgan.spectralAnalysis.AudioConditionerConfig
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import math.normalization.UnitInterval
import music.WesternTuningSystem
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import serial.SerialFrameFormat
import toolkit.assertions.assertRgbEquals
import kotlin.time.Duration.Companion.seconds

@OptIn(ExperimentalCoroutinesApi::class)
class LightOrganIntegrationTests {

    private val minimalConfig = AppConfig(
        spectralAnalysis = SpectralAnalysisConfig(
            audioConditioner = AudioConditionerConfig(
                gainDb = 0f,
                highPassFilter = null,
                lowPassFilter = null,
                rolloffThreshold = -48f,
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

    private lateinit var fakeAudioInputManager: FakeAudioInputManager
    private lateinit var fakeGatewayManager: FakeGatewayManager

    private val westernTuning = WesternTuningSystem()
    private val cFrequency = westernTuning.getFrequency(westernTuning.C, 4)
    private val fSharpFrequency = westernTuning.getFrequency(westernTuning.F_SHARP, 4)

    @BeforeEach
    fun setup() {
        fakeAudioInputManager = FakeAudioInputManager()
        fakeGatewayManager = FakeGatewayManager()

        AppConfigSingleton.value = minimalConfig
    }

    private fun createSUT(scope: CoroutineScope): LightOrgan {
        return LightOrgan(
            inputManager = fakeAudioInputManager,
            spectralAnalyzer = SpectralAnalyzer(),
            colorCalculator = ColorCalculator(),
            gatewayManager = fakeGatewayManager,
            scope = scope
        )
    }

    @Test
    fun `when a C note is played, then red is displayed`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        fakeGatewayManager.connect()
        runCurrent()

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        val red = StandardRgbColor(red = UnitInterval.one, green = UnitInterval.zero, blue = UnitInterval.zero)
        assertRgbEquals(red, fakeGatewayManager.gateway.lastColor!!, 0.01)
    }

    @Test
    fun `when an F# note is played, then cyan is displayed`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        fakeGatewayManager.connect()
        runCurrent()

        fakeAudioInputManager.emitTone(fSharpFrequency)
        runCurrent()

        val cyan = StandardRgbColor(red = UnitInterval.zero, green = UnitInterval.one, blue = UnitInterval.one)
        assertRgbEquals(cyan, fakeGatewayManager.gateway.lastColor!!, 0.01)
    }

}