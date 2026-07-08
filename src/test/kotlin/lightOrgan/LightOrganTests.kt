package lightOrgan

import io.mockk.clearAllMocks
import io.mockk.every
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import lightOrgan.color.ColorManagerFixture
import lightOrgan.gateway.FakeGatewayManager
import lightOrgan.input.AudioInputManagerFixture
import lightOrgan.spectralAnalysis.SpectralAnalyzerFixture
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextAppConfig
import toolkit.monkeyTest.nextAudioFrame
import toolkit.monkeyTest.nextSpectralAnalysis
import toolkit.monkeyTest.nextStandardRgbColor
import utilities.coroutines.asLazySequence

@OptIn(ExperimentalCoroutinesApi::class)
class LightOrganTests {

    private lateinit var inputManager: AudioInputManagerFixture
    private lateinit var spectralAnalyzer: SpectralAnalyzerFixture
    private lateinit var colorManager: ColorManagerFixture
    private lateinit var fakeGatewayManager: FakeGatewayManager

    private val config = nextAppConfig()
    private val audioFrame = nextAudioFrame()
    private val spectralAnalysis = nextSpectralAnalysis()
    private val newColor = nextStandardRgbColor()

    @BeforeEach
    fun setupHappyPath() {
        inputManager = AudioInputManagerFixture.create()
        spectralAnalyzer = SpectralAnalyzerFixture.create()
        colorManager = ColorManagerFixture.create()
        fakeGatewayManager = FakeGatewayManager()

        every { spectralAnalyzer.mock.analyze(audioFrame, config.spectralAnalysis) } returns spectralAnalysis
        every { colorManager.mock.calculate(spectralAnalysis) } returns newColor
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(scope: CoroutineScope): LightOrgan {
        return LightOrgan(
            inputManager = inputManager.mock,
            spectralAnalyzer = spectralAnalyzer.mock,
            colorManager = colorManager.mock,
            gatewayManager = fakeGatewayManager,
            scope = scope,
        )
    }

    @Test
    fun `when an audio frame is received, then a color is derived and broadcast`() = runTest {
        val sut = createSUT(backgroundScope)
        fakeGatewayManager.connect()
        sut.start({ config })
        runCurrent()

        inputManager.audioStream.emit(audioFrame.asLazySequence())
        runCurrent()

        assertEquals(newColor, fakeGatewayManager.gateway.lastColor)
    }

}
