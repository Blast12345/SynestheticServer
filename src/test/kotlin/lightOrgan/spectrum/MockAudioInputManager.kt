package lightOrgan.spectrum

import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import lightOrgan.spectralAnalysis.SpectralAnalysis
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculator

data class SpectrumManagerFixture(
    val mock: SpectrumCalculator,
    val spectralAnalysis: MutableStateFlow<SpectralAnalysis>
) {

    companion object {
        fun create(): SpectrumManagerFixture {
            val fixture = SpectrumManagerFixture(
                mock = mockk<SpectrumCalculator>(),
                spectralAnalysis = MutableStateFlow(SpectralAnalysis.EMPTY)
            )

            every { fixture.mock.spectralAnalysis } returns fixture.spectralAnalysis

            return fixture
        }
    }

}