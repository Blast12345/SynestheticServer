package lightOrgan.spectralAnalysis

import audio.samples.AudioFrame
import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import lightOrgan.spectralAnalysis.conditioning.AudioConditioner
import lightOrgan.spectralAnalysis.noiseReduction.NoiseGate
import lightOrgan.spectralAnalysis.peaks.PeakExtractor
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculator

// TODO: Test me
// ENHANCEMENT: Split between short duration FFT (for brightness) and long duration FFT (for hue).
class SpectralAnalyzer(
    private val audioConditioner: AudioConditioner = AudioConditioner(),
    private val spectrumCalculator: SpectrumCalculator = SpectrumCalculator(),
    private val peakExtractor: PeakExtractor = PeakExtractor(),
    private val noiseGate: NoiseGate = NoiseGate()
) {

    private val _analysis = MutableStateFlow(SpectralAnalysis.EMPTY)
    val analysis: StateFlow<SpectralAnalysis> = _analysis.asStateFlow()

    // WARNING: Discontinuous data will cause spectral artifacts
    fun analyze(audio: AudioFrame): SpectralAnalysis {
        val conditionedAudio = audioConditioner.condition(audio)
        val spectrum = spectrumCalculator.calculate(conditionedAudio)
        val peaks = peakExtractor.extract(spectrum)

        _analysis.value = SpectralAnalysis(
            spectrum = spectrum.passband().denoise(),
            peaks = peaks.passband().denoise(),
        )

        return _analysis.value
    }

    @JvmName("passbandSpectrum")
    private fun FrequencyBins.passband(): FrequencyBins = filter { it.frequency in audioConditioner.passband }

    @JvmName("passbandPeaks")
    private fun SpectralPeaks.passband(): SpectralPeaks = filter { it.frequency in audioConditioner.passband }

    @JvmName("denoiseSpectrum")
    private fun FrequencyBins.denoise(): FrequencyBins = noiseGate.apply(this)

    @JvmName("denoisePeaks")
    private fun SpectralPeaks.denoise(): SpectralPeaks = noiseGate.apply(this)

}