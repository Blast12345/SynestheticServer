package lightOrgan.spectralAnalysis

import audio.samples.AudioFrame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lightOrgan.spectralAnalysis.conditioning.AudioConditioner
import lightOrgan.spectralAnalysis.peaks.PeakExtractor
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculator

// TODO: Test me
class SpectralAnalyzer(
    private val audioConditioner: AudioConditioner = AudioConditioner(),
    private val spectrumCalculator: SpectrumCalculator = SpectrumCalculator(),
    private val peakExtractor: PeakExtractor = PeakExtractor()
) {

    private val _analysis = MutableStateFlow(SpectralAnalysis.EMPTY)
    val analysis: StateFlow<SpectralAnalysis> = _analysis.asStateFlow()

    // WARNING: Discontinuous data will cause spectral artifacts
    fun analyze(audio: AudioFrame): SpectralAnalysis {
        val conditionedAudio = audioConditioner.condition(audio)
        val spectrum = spectrumCalculator.calculate(conditionedAudio)
        val relevantBins = spectrum.bins.filter { it.frequency in audioConditioner.passband }
        val peaks = peakExtractor.extract(relevantBins, spectrum.pointSpreadFunction)

        val result = SpectralAnalysis(relevantBins, peaks)
        _analysis.value = result
        return result
    }

}