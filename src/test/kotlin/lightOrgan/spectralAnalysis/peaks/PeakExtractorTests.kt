package lightOrgan.spectralAnalysis.peaks

import dsp.bins.FrequencyBins
import dsp.peakExtraction.ParabolicSpectralPeakExtractor
import dsp.peakExtraction.SpectralPeakExtractor
import dsp.peakExtraction.SpectralPeaks
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextFrequencyBins
import toolkit.monkeyTest.nextSpectralAnalysisConfig
import toolkit.monkeyTest.nextSpectralPeaks

class PeakExtractorTests {

    private val config = nextSpectralAnalysisConfig()
    private val parabolicExtractor: ParabolicSpectralPeakExtractor = mockk()

    private val spectrum: FrequencyBins = nextFrequencyBins()
    private val peaks: SpectralPeaks = nextSpectralPeaks()

    @BeforeEach
    fun setupHappyPath() {
        every { parabolicExtractor.extract(spectrum) } returns peaks
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(forcedExtractor: SpectralPeakExtractor): PeakExtractor {
        return PeakExtractor(
            config = config,
            extractor = forcedExtractor
        )
    }

    @Test
    fun `parabolic extraction`() {
        val sut = createSUT(parabolicExtractor)

        val actual = sut.extract(spectrum)

        verify { parabolicExtractor.extract(spectrum) }
        assertEquals(peaks, actual)
    }

}