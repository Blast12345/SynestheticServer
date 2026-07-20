package lightOrgan

import color.StandardRgbColors
import config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import lightOrgan.color.ColorCalculator
import lightOrgan.gateway.FakeGatewayManager
import lightOrgan.input.FakeAudioInputManager
import lightOrgan.spectralAnalysis.SpectralAnalysis
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import music.WesternTuningSystem
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import toolkit.minimalAppConfig

@OptIn(ExperimentalCoroutinesApi::class)
class LightOrganIntegrationTests {

    private lateinit var fakeAudioInputManager: FakeAudioInputManager
    private lateinit var fakeGatewayManager: FakeGatewayManager

    private val westernTuning = WesternTuningSystem()
    private val cFrequency = westernTuning.getFrequency(westernTuning.C, 4)
    private val fSharpFrequency = westernTuning.getFrequency(westernTuning.F_SHARP, 4)

    private val frequencyTolerance = minimalAppConfig.spectralAnalysis.approximateBinSpacing

    @BeforeEach
    fun setup() {
        fakeAudioInputManager = FakeAudioInputManager()
        fakeGatewayManager = FakeGatewayManager()
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

    private fun TestScope.startLightOrgan(config: AppConfig = minimalAppConfig): LightOrgan {
        val sut = createSUT(backgroundScope)
        sut.start(config = { config })
        runCurrent()
        return sut
    }

    // Spectral analysis
    @Test
    fun `before any audio arrives, the analysis is empty`() = runTest {
        val sut = startLightOrgan(minimalAppConfig)

        assertEquals(SpectralAnalysis.EMPTY, sut.spectralAnalysis.value)
    }

    @Test
    fun `when a tone is played, then the dominant bin is near that frequency`() = runTest {
        val sut = startLightOrgan(minimalAppConfig)

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        val dominantBin = sut.spectralAnalysis.value.spectrum.maxBy { it.magnitude }
        assertEquals(cFrequency, dominantBin.frequency, frequencyTolerance)
    }

    @Test
    fun `when a tone is played, then the dominant peak is near that frequency`() = runTest {
        val sut = startLightOrgan(minimalAppConfig)

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        val dominantPeak = sut.spectralAnalysis.value.peaks.maxBy { it.magnitude }
        assertEquals(cFrequency, dominantPeak.frequency, frequencyTolerance)
    }

    @Test
    fun `when a second tone is played, then the analysis reflects the new tone`() = runTest {
        val sut = startLightOrgan(minimalAppConfig)

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        fakeAudioInputManager.emitTone(fSharpFrequency)
        runCurrent()

        val dominantPeak = sut.spectralAnalysis.value.peaks.maxBy { it.magnitude }
        assertEquals(fSharpFrequency, dominantPeak.frequency, frequencyTolerance)
    }

    // Color flow
    @Test
    fun `before any audio arrives, the color is black`() = runTest {
        val sut = startLightOrgan(minimalAppConfig)

        assertEquals(StandardRgbColors.Black, sut.color.value)
    }

    @Test
    fun `when a tone is played, then a color is emitted`() = runTest {
        val sut = startLightOrgan(minimalAppConfig)

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        assertNotEquals(StandardRgbColors.Black, sut.color.value)
    }

    @Test
    fun `when a second tone is played, then the color changes`() = runTest {
        val sut = startLightOrgan(minimalAppConfig)

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
        val sut = startLightOrgan(minimalAppConfig)

        fakeGatewayManager.connect()
        runCurrent()

        assertNull(fakeGatewayManager.gateway.lastColor)
    }

    @Test
    fun `when a tone is played, then the current color is broadcast`() = runTest {
        val sut = startLightOrgan(minimalAppConfig)
        fakeGatewayManager.connect()
        runCurrent()

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        assertEquals(sut.color.value, fakeGatewayManager.gateway.lastColor)
    }

    @Test
    fun `when a gateway connects after audio has started, then subsequent colors are broadcast`() = runTest {
        val sut = startLightOrgan(minimalAppConfig)

        fakeAudioInputManager.emitTone(cFrequency)
        runCurrent()

        fakeGatewayManager.connect()
        fakeAudioInputManager.emitTone(fSharpFrequency)
        runCurrent()

        assertEquals(sut.color.value, fakeGatewayManager.gateway.lastColor)
    }

}