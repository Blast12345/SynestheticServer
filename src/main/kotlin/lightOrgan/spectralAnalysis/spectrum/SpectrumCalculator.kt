package lightOrgan.spectralAnalysis.spectrum

import audio.samples.AudioFrame
import audio.samples.RollingAudioBuffer
import dsp.ZeroPaddingInterpolator
import dsp.bins.FftFrequencyBinsCalculator
import dsp.bins.FrequencyBins
import dsp.bins.FrequencyBinsCalculator
import dsp.windowing.Window
import dsp.windowing.WindowFactory
import extensions.inSeconds
import math.nextPowerOfTwo

// ENHANCEMENT: Spectral "reassignment method"
// ENHANCEMENT: Multi-resolution bin generations
// ENHANCEMENT: Implement equal-loudness contours (ISO 226:2003). Manual SPL number with future plans of external meter?
class SpectrumCalculator(
    private val audioBuffer: RollingAudioBuffer = RollingAudioBuffer(),
    private val windowFactory: WindowFactory = WindowFactory(),
    private val interpolator: ZeroPaddingInterpolator = ZeroPaddingInterpolator(),
    private val frequencyBinsCalculator: FrequencyBinsCalculator = FftFrequencyBinsCalculator(),
) {

    fun calculate(audio: AudioFrame, config: SpectrumCalculatorConfig): FrequencyBins {
        val window = windowFactory.create(config.window)

        val sampleSize = (config.frameDuration.inSeconds * audio.format.sampleRate).toInt()
        val samplesSizeForDesiredSpacing = (audio.format.sampleRate / config.approximateBinSpacing).toInt()
        val fftLength = nextPowerOfTwo(samplesSizeForDesiredSpacing)

        val bufferedAudio = updateBuffer(audio, sampleSize)
        val windowedSamples = window.appliedTo(bufferedAudio.samples, Window.CorrectionType.MAGNITUDE)
        val interpolated = interpolator.interpolate(windowedSamples, fftLength)
        val allBins = frequencyBinsCalculator.calculate(interpolated, audio.format.sampleRate)

        val lowestFrequency = config.frequencyResolution
        val highestFrequency = audio.format.nyquistFrequency
        val validBins = allBins.filter { it.frequency in lowestFrequency..<highestFrequency }

        return validBins
    }

    private fun updateBuffer(frame: AudioFrame, requiredSize: Int): AudioFrame {
        audioBuffer.size = requiredSize
        return audioBuffer.append(frame)
    }

}