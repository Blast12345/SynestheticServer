package lightOrgan.spectralAnalysis

import audio.samples.AudioFrame
import lightOrgan.spectralAnalysis.conditioning.AudioConditioner
import lightOrgan.spectralAnalysis.peaks.PeakExtractor
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculator

// ENHANCEMENT: Split between short duration FFT (for brightness) and long duration FFT (for hue).
class SpectralAnalyzer(
    private val audioConditioner: AudioConditioner = AudioConditioner(),
    private val spectrumCalculator: SpectrumCalculator = SpectrumCalculator(),
    private val peakExtractor: PeakExtractor = PeakExtractor()
) {

    // WARNING: Discontinuous data will cause spectral artifacts
    fun analyze(
        audio: AudioFrame,
        config: SpectralAnalysisConfig
    ): SpectralAnalysis {
        val conditionedAudio = audioConditioner.condition(audio, config.audioConditioner)
        val spectrum = spectrumCalculator.calculate(conditionedAudio)
        val peaks = peakExtractor.extract(spectrum)

        return SpectralAnalysis(
            spectrum = spectrum.filter { it.frequency in config.audioConditioner.passband },
            peaks = peaks.filter { it.frequency in config.audioConditioner.passband },
        )
    }

}