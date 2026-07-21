package lightOrgan.spectralAnalysis.spectrum

import audio.samples.AudioFrame
import audio.samples.RollingAudioBuffer
import dsp.ZeroPaddingInterpolator
import dsp.bins.FftFrequencyBinsCalculator
import dsp.bins.FrequencyBins
import dsp.bins.FrequencyBinsCalculator
import dsp.windowing.Window
import dsp.windowing.WindowFactory
import dsp.windowing.WindowType
import extensions.inSeconds
import math.nextPowerOfTwo
import kotlin.time.Duration

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
        val buffered = updateBuffer(audio, config.frameDuration)
        val windowed = applyWindow(buffered, config.window)
        val interpolated = interpolate(windowed, config.approximateBinSpacing)
        val allBins = calculateAllBins(interpolated)

        return allBins.findValid(
            frequencyResolution = config.frequencyResolution,
            nyquistFrequency = audio.format.nyquistFrequency
        )
    }

    private fun updateBuffer(frame: AudioFrame, frameDuration: Duration): AudioFrame {
        val sampleSize = (frameDuration.inSeconds * frame.format.sampleRate).toInt()
        audioBuffer.size = sampleSize
        return audioBuffer.append(frame)
    }

    private fun applyWindow(frame: AudioFrame, type: WindowType): AudioFrame {
        val window = windowFactory.create(type)
        val samples = window.appliedTo(frame.samples, Window.CorrectionType.MAGNITUDE)
        return AudioFrame(samples, frame.format)
    }

    private fun interpolate(frame: AudioFrame, approximateBinSpacing: Float): AudioFrame {
        val samplesSizeForDesiredSpacing = (frame.format.sampleRate / approximateBinSpacing).toInt()
        val fftLength = nextPowerOfTwo(samplesSizeForDesiredSpacing)
        val samples = interpolator.interpolate(frame.samples, fftLength)
        return AudioFrame(samples, frame.format)
    }

    private fun calculateAllBins(frame: AudioFrame): FrequencyBins {
        return frequencyBinsCalculator.calculate(frame.samples, frame.format.sampleRate)
    }

    private fun FrequencyBins.findValid(frequencyResolution: Float, nyquistFrequency: Float): FrequencyBins {
        return filter { it.frequency in frequencyResolution..<nyquistFrequency }
    }

}