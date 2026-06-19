package lightOrgan.spectralAnalysis

import audio.samples.AudioFrame
import config.ConfigSingleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lightOrgan.spectralAnalysis.conditioning.AudioConditioner
import lightOrgan.spectralAnalysis.peaks.PeakExtractor
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculator

class SpectralAnalyzer(
    private val config: SpectralAnalysisConfig = ConfigSingleton.spectralAnalysis,
    private val audioConditioner: AudioConditioner = AudioConditioner(config),
    private val spectrumCalculator: SpectrumCalculator = SpectrumCalculator(config),
    private val peakExtractor: PeakExtractor = PeakExtractor(config)
) {

    private val _analysis = MutableStateFlow(SpectralAnalysis.EMPTY)
    val analysis: StateFlow<SpectralAnalysis> = _analysis.asStateFlow()

    // WARNING: Discontinuous data will cause spectral artifacts
    fun analyze(audio: AudioFrame): SpectralAnalysis {
        val conditionedAudio = audioConditioner.condition(audio)
        val spectrum = spectrumCalculator.calculate(conditionedAudio)
        val peaks = peakExtractor.extract(spectrum)

        _analysis.value = SpectralAnalysis(
            spectrum = spectrum.filter { it.frequency in audioConditioner.passband },
            peaks = peaks.filter { it.frequency in audioConditioner.passband },
        )

        return _analysis.value
    }

}