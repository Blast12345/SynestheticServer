package lightOrgan.spectralAnalysis.spectrum

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import audio.samples.RollingAudioBuffer
import dsp.ZeroPaddingInterpolator
import dsp.bins.FftFrequencyBinsCalculator
import dsp.bins.FrequencyBins
import dsp.bins.FrequencyBinsCalculator
import dsp.windowing.Window
import extensions.inSeconds
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import math.nextPowerOfTwo

// ENHANCEMENT: Spectral "reassignment method"
// ENHANCEMENT: Multi-resolution bin generations
// ENHANCEMENT: Implement equal-loudness contours (ISO 226:2003). Manual SPL number with future plans of external meter?
class SpectrumCalculator(
    private val config: SpectralAnalysisConfig,
    private val audioBuffer: RollingAudioBuffer = RollingAudioBuffer(),
    private val window: Window = config.window.createWindow(),
    private val interpolator: ZeroPaddingInterpolator = ZeroPaddingInterpolator(),
    private val frequencyBinsCalculator: FrequencyBinsCalculator = FftFrequencyBinsCalculator(),
) {

    private val frequencyResolution = 1 / config.frameDuration.inSeconds

    fun calculate(audio: AudioFrame): FrequencyBins {
        val sampleSize = (config.frameDuration.inSeconds * audio.format.sampleRate).toInt()
        val samplesSizeForDesiredSpacing = (audio.format.sampleRate / config.approximateBinSpacing).toInt()
        val fftLength = nextPowerOfTwo(samplesSizeForDesiredSpacing)

        val bufferedAudio = updateBuffer(audio, sampleSize)
        val windowedSamples = window.appliedTo(bufferedAudio.samples, Window.CorrectionType.MAGNITUDE)
        val interpolated = interpolator.interpolate(windowedSamples, fftLength)
        val bins = frequencyBinsCalculator.calculate(interpolated, audio.format.sampleRate)
        val validBins = filterBins(bins, audio.format)

        return validBins
    }

    private fun updateBuffer(frame: AudioFrame, requiredSize: Int): AudioFrame {
        audioBuffer.size = requiredSize
        return audioBuffer.append(frame)
    }

    private fun filterBins(bins: FrequencyBins, format: AudioFormat): FrequencyBins {
        val lowestFrequency = frequencyResolution
        val highestFrequency = format.nyquistFrequency

        return bins.filter { it.frequency in lowestFrequency..<highestFrequency }
    }

}