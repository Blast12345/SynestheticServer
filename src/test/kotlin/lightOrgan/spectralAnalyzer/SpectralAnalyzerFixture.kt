package lightOrgan.spectralAnalyzer

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.spectralAnalysis.SpectralAnalysis
import lightOrgan.spectralAnalysis.SpectralAnalyzer

data class SpectralAnalyzerFixture(
    val mock: SpectralAnalyzer,
    val spectralAnalysis: MutableStateFlow<SpectralAnalysis>
) {

    companion object {
        fun create(): SpectralAnalyzerFixture {
            val fixture = SpectralAnalyzerFixture(
                mock = mockk(),
                spectralAnalysis = MutableStateFlow(SpectralAnalysis.EMPTY)
            )

            every { fixture.mock.analysis } returns fixture.spectralAnalysis

            return fixture
        }
    }

}