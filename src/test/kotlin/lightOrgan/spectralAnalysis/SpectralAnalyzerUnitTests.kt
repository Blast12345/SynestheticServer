package lightOrgan.spectralAnalysis

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import lightOrgan.spectralAnalysis.conditioning.AudioConditioner
import lightOrgan.spectralAnalysis.peaks.PeakExtractor
import lightOrgan.spectralAnalysis.postProcessing.SpectralPostProcessor
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextAudioFrame
import toolkit.monkeyTest.nextFrequencyBins
import toolkit.monkeyTest.nextSpectralAnalysisConfig
import toolkit.monkeyTest.nextSpectralPeaks

class SpectralAnalyzerUnitTests {

    private val audioConditioner: AudioConditioner = mockk()
    private val spectrumCalculator: SpectrumCalculator = mockk()
    private val peakExtractor: PeakExtractor = mockk()
    private val postProcessor: SpectralPostProcessor = mockk()

    private val audioFrame = nextAudioFrame()
    private val config: SpectralAnalysisConfig = nextSpectralAnalysisConfig()

    private val conditionedAudio = nextAudioFrame()
    private val rawSpectrum = nextFrequencyBins()
    private val processedSpectrum = nextFrequencyBins()
    private val allPeaks = nextSpectralPeaks()
    private val processedPeaks = nextSpectralPeaks()

    @BeforeEach
    fun setupHappyPath() {
        every { audioConditioner.condition(audioFrame, config.audioConditioner) } returns conditionedAudio
        every { spectrumCalculator.calculate(conditionedAudio) } returns rawSpectrum
        every { peakExtractor.extract(rawSpectrum) } returns allPeaks
        every { postProcessor.processSpectrum(rawSpectrum, config) } returns processedSpectrum
        every { postProcessor.processPeaks(allPeaks, config) } returns processedPeaks
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(): SpectralAnalyzer {
        return SpectralAnalyzer(
            audioConditioner,
            spectrumCalculator,
            peakExtractor,
            postProcessor
        )
    }


    @Test
    fun `peaks are extracted from the raw spectrum`() {
        // This ensures that downstream interpolation is accurate

        val sut = createSUT()

        sut.analyze(audioFrame, config)

        verify { peakExtractor.extract(rawSpectrum) }
    }

    // Flow
    @Test
    fun `initial analysis is empty`() {
        val sut = createSUT()

        assertEquals(SpectralAnalysis.EMPTY, sut.analysis.value)
    }

    @Test
    fun `when audio is analyzed, then the flow is updated`() {
        val sut = createSUT()

        val actual = sut.analyze(audioFrame, config)

        assertEquals(actual, sut.analysis.value)
    }

}