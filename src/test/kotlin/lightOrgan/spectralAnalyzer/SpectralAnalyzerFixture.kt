package lightOrgan.spectralAnalyzer

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

data class SpectralAnalyzerFixture(
    val mock: SpectralAnalyzer,
    val analysis: MutableStateFlow<SpectralAnalysis>
) {

    companion object {
        fun create(): SpectralAnalyzerFixture {
            val fixture = SpectralAnalyzerFixture(
                mock = mockk(),
                analysis = MutableStateFlow(SpectralAnalysis.EMPTY)
            )

            every { fixture.mock.analysis } returns fixture.analysis

            return fixture
        }
    }

}