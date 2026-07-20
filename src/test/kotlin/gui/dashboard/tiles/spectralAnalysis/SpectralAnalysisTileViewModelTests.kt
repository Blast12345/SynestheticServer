package gui.dashboard.tiles.spectralAnalysis

import dsp.bins.FrequencyBin
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.spectralAnalysis.SpectralAnalysis
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class SpectralAnalysisTileViewModelTests {

    private val analysis = MutableStateFlow(SpectralAnalysis.EMPTY)

    private fun createSUT(): SpectralAnalysisTileViewModel {
        return SpectralAnalysisTileViewModel(analysis)
    }

    private fun analysisWithBins(count: Int): SpectralAnalysis {
        val spectrum = List(count) { index ->
            FrequencyBin(frequency = 100f * (index + 1), magnitude = 1.0)
        }
        return SpectralAnalysis(spectrum = spectrum, peaks = emptyList())
    }

    // Highlighted bin
    @Test
    fun `given no highlighted index, then there is no highlighted bin`() {
        val sut = createSUT()
        analysis.value = analysisWithBins(3)

        assertNull(sut.highlightedBin)
    }

    @Test
    fun `given a highlighted index, then the highlighted bin is the bin at that index`() {
        val sut = createSUT()
        analysis.value = analysisWithBins(3)

        sut.highlightedIndex = 1

        assertEquals(analysis.value.spectrum[1], sut.highlightedBin)
    }

    @Test
    fun `given an out of bounds highlighted index, then there is no highlighted bin`() {
        val sut = createSUT()
        analysis.value = analysisWithBins(3)

        sut.highlightedIndex = 5

        assertNull(sut.highlightedBin)
    }

    @Test
    fun `when the spectrum shrinks below the highlighted index, then there is no highlighted bin`() {
        val sut = createSUT()
        analysis.value = analysisWithBins(3)
        sut.highlightedIndex = 2

        analysis.value = analysisWithBins(1)

        assertNull(sut.highlightedBin)
    }

}
