package lightOrgan.spectrum

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow

data class SpectrumManagerFixture(
    val mock: SpectrumManager,
    val spectralAnalysis: MutableStateFlow<SpectralAnalysis>
) {

    companion object {
        fun create(): SpectrumManagerFixture {
            val fixture = SpectrumManagerFixture(
                mock = mockk<SpectrumManager>(),
                spectralAnalysis = MutableStateFlow(SpectralAnalysis.EMPTY)
            )

            every { fixture.mock.spectralAnalysis } returns fixture.spectralAnalysis

            return fixture
        }
    }

}