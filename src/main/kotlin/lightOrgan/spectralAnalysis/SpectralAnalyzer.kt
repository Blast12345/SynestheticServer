package lightOrgan.spectralAnalysis

import audio.samples.AudioFrame
import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks
import lightOrgan.spectralAnalysis.conditioning.AudioConditioner
import lightOrgan.spectralAnalysis.noiseReduction.NoiseReducerFactory
import lightOrgan.spectralAnalysis.peaks.PeakExtractor
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculator

// ENHANCEMENT: Split between short duration FFT (for brightness) and long duration FFT (for hue).
class SpectralAnalyzer(
    private val audioConditioner: AudioConditioner = AudioConditioner(),
    private val spectrumCalculator: SpectrumCalculator = SpectrumCalculator(),
    private val peakExtractor: PeakExtractor = PeakExtractor(),
    private val noiseReducerFactory: NoiseReducerFactory = NoiseReducerFactory()
) {
    // WARNING: Discontinuous data will cause spectral artifacts
    fun analyze(
        audio: AudioFrame,
        config: SpectralAnalysisConfig
    ): SpectralAnalysis {
        val conditionedAudio = audioConditioner.condition(audio, config.audioConditioner)
        val rawSpectrum = spectrumCalculator.calculate(conditionedAudio, config.spectrumCalculator)
        val rawPeaks = peakExtractor.extract(rawSpectrum)

        return SpectralAnalysis(
            spectrum = processSpectrum(rawSpectrum, config.postProcessor),
            peaks = processPeaks(rawPeaks, config.postProcessor),
        )
    }

    private fun processSpectrum(spectrum: FrequencyBins, config: PostProcessorConfig): FrequencyBins {
        var processed = spectrum

        if (config.noiseReducer != null) {
            val noiseReducer = noiseReducerFactory.create(config.noiseReducer)
            processed = noiseReducer.reduceSpectrum(processed)
        }

        return processed
    }

    private fun processPeaks(peaks: SpectralPeaks, config: PostProcessorConfig): SpectralPeaks {
        var processed = peaks

        if (config.noiseReducer != null) {
            val noiseReducer = noiseReducerFactory.create(config.noiseReducer)
            processed = noiseReducer.reducePeaks(processed)
        }

        return processed
    }

}