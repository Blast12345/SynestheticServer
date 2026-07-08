package lightOrgan.spectralAnalysis

import audio.samples.AudioFrame
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lightOrgan.spectralAnalysis.conditioning.AudioConditioner
import lightOrgan.spectralAnalysis.peaks.PeakExtractor
import lightOrgan.spectralAnalysis.postProcessing.SpectralPostProcessor
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculator

// TODO: Test me
// ENHANCEMENT: Split between short duration FFT (for brightness) and long duration FFT (for hue).
class SpectralAnalyzer(
    private val audioConditioner: AudioConditioner = AudioConditioner(),
    private val spectrumCalculator: SpectrumCalculator = SpectrumCalculator(),
    private val peakExtractor: PeakExtractor = PeakExtractor(),
    private val postProcessor: SpectralPostProcessor = SpectralPostProcessor()
) {

    private val _analysis = MutableStateFlow(SpectralAnalysis.EMPTY)
    val analysis: StateFlow<SpectralAnalysis> = _analysis.asStateFlow()

    // WARNING: Discontinuous data will cause spectral artifacts
    fun analyze(
        audio: AudioFrame,
        config: SpectralAnalysisConfig
    ): SpectralAnalysis {
        val conditionedAudio = audioConditioner.condition(audio, config.audioConditioner)
        val rawSpectrum = spectrumCalculator.calculate(conditionedAudio)
        val rawPeaks = peakExtractor.extract(rawSpectrum)

        val analysis = SpectralAnalysis(
            spectrum = postProcessor.processSpectrum(rawSpectrum, config),
            peaks = postProcessor.processPeaks(rawPeaks, config),
        )

        _analysis.value = analysis
        return analysis
    }

}