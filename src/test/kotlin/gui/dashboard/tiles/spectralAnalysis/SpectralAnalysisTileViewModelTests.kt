//package gui.dashboard.tiles.spectralAnalysis
//
//import io.mockk.clearAllMocks
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import lightOrgan.spectrum.SpectrumManagerFixture
//import org.junit.jupiter.api.AfterEach
//import org.junit.jupiter.api.Assertions.assertEquals
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import toolkit.monkeyTest.nextSpectralAnalysis
//
//@OptIn(ExperimentalCoroutinesApi::class)
//class SpectralAnalysisTileViewModelTests {
//
//    private lateinit var spectrumManager: SpectrumManagerFixture
//
//    private val spectralAnalysis = nextSpectralAnalysis()
//
//    @BeforeEach
//    fun setupHappyPath() {
//        spectrumManager = SpectrumManagerFixture.create()
//    }
//
//    @AfterEach
//    fun tearDown() {
//        clearAllMocks()
//    }
//
//    private fun createSUT(): SpectralAnalysisTileViewModel {
//        return SpectralAnalysisTileViewModel(
//            spectrumCalculator = spectrumManager.mock
//        )
//    }
//
//    @Test
//    fun `when new frequency bins are available, then update the displayed spectrum`() {
//        val sut = createSUT()
//
//        spectrumManager.spectralAnalysis.value = spectralAnalysis
//
//        assertEquals(spectralAnalysis.spectrum, sut.analysis.value.spectrum)
//    }
//
//    @Test
//    fun `highlight a frequency bin`() {
//        val sut = createSUT()
//        spectrumManager.spectralAnalysis.value = spectralAnalysis
//
//        val randomBin = spectralAnalysis.spectrum.random()
//        val binIndex = spectralAnalysis.spectrum.indexOf(randomBin)
//        sut.highlightedIndex = binIndex
//
//        assertEquals(randomBin, sut.highlightedBin)
//    }
//
//}
