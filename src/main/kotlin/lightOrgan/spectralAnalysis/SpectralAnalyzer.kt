package lightOrgan.spectralAnalysis

import audio.samples.AudioFrame
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
    // WARNING: Discontinuous data will cause spectral artifacts
    fun analyze(
        audio: AudioFrame,
        config: SpectralAnalysisConfig
    ): SpectralAnalysis {
        val conditionedAudio = audioConditioner.condition(audio, config.audioConditioner)
        val rawSpectrum = spectrumCalculator.calculate(conditionedAudio)
        val rawPeaks = peakExtractor.extract(rawSpectrum)

        return SpectralAnalysis(
            spectrum = postProcessor.processSpectrum(rawSpectrum, config.audioConditioner.passband, config.noiseReducer),
            peaks = postProcessor.processPeaks(rawPeaks, config.audioConditioner.passband, config.noiseReducer),
        )
    }

}