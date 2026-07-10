package lightOrgan

import color.StandardRgbColors
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
import lightOrgan.spectralAnalysis.SpectralAnalysis
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import music.WesternTuningSystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import serial.SerialFrameFormat
import kotlin.time.Duration.Companion.seconds

// These are high level tests that validate things that should generally be true regardless of config.
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

    // Spectral analysis
    @Test
    fun `before any audio arrives, the analysis is empty`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        runCurrent()

        assertEquals(SpectralAnalysis.EMPTY, sut.spectralAnalysis.value)
    }

    @Test
    fun `when a tone is played, then the dominant bin is near that frequency`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        runCurrent()

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        val dominantBin = sut.spectralAnalysis.value.spectrum.maxByOrNull { it.magnitude }!!
        assertEquals(cFrequency, dominantBin.frequency, minimalConfig.spectralAnalysis.approximateBinSpacing)
    }

    @Test
    fun `when a tone is played, then the dominant peak is near that frequency`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        runCurrent()

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        val dominantPeak = sut.spectralAnalysis.value.peaks.maxByOrNull { it.magnitude }!!
        assertEquals(cFrequency, dominantPeak.frequency, minimalConfig.spectralAnalysis.approximateBinSpacing)
    }

    @Test
    fun `when a second tone is played, then the analysis reflects the new tone`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        runCurrent()

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        fakeAudioInputManager.emitTone(fSharpFrequency)
        runCurrent()

        val dominantPeak = sut.spectralAnalysis.value.peaks.maxByOrNull { it.magnitude }!!
        assertEquals(fSharpFrequency, dominantPeak.frequency, minimalConfig.spectralAnalysis.approximateBinSpacing)
    }

    // Color flow
    @Test
    fun `before any audio arrives, the color is black`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        runCurrent()

        assertEquals(StandardRgbColors.Black, sut.color.value)
    }

    @Test
    fun `when tone is played, then a color is emitted`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        runCurrent()

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        assertNotEquals(StandardRgbColors.Black, sut.color.value)
    }

    @Test
    fun `when a second tone is played, then the color changes`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        runCurrent()

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()
        val firstColor = sut.color.value

        fakeAudioInputManager.emitTone(fSharpFrequency)
        runCurrent()
        val secondColor = sut.color.value

        assertNotEquals(firstColor, secondColor)
    }

    // Broadcast
    @Test
    fun `before any audio arrives, nothing was broadcast`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        fakeGatewayManager.connect()
        runCurrent()

        assertNull(fakeGatewayManager.gateway.lastColor)
    }

    @Test
    fun `when a tone is played, then the current color is broadcast`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        fakeGatewayManager.connect()
        runCurrent()

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        assertEquals(sut.color.value, fakeGatewayManager.gateway.lastColor)
    }

    @Test
    fun `when a gateway connects after audio has started, then subsequent colors are broadcast`() = runTest {
        val sut = createSUT(backgroundScope)
        sut.start()
        runCurrent()
        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        fakeGatewayManager.connect()
        fakeAudioInputManager.emitTone(fSharpFrequency)
        runCurrent()

        assertEquals(sut.color.value, fakeGatewayManager.gateway.lastColor)
    }

}