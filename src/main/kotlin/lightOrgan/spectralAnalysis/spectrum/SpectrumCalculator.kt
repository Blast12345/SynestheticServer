package lightOrgan.spectralAnalysis.spectrum

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import audio.samples.RollingAudioBuffer
import config.ConfigSingleton
import dsp.ZeroPaddingInterpolator
import dsp.bins.FftFrequencyBinsCalculator
import dsp.bins.FrequencyBins
import dsp.bins.FrequencyBinsCalculator
import dsp.peakExtraction.PointSpreadFunction
import dsp.peakExtraction.PsfCalculator
import dsp.windowing.Window
import extensions.inSeconds
import math.nextPowerOfTwo

// ENHANCEMENT: Multi-resolution bin generations
// ENHANCEMENT: Implement equal-loudness contours (ISO 226:2003). Manual SPL number with future plans of external meter?
// ENHANCEMENT: If implementing other calculation strategies (e.g., DFT, CZT), then create a bin calculator interface
// ENHANCEMENT: Explore sub-frame duration frequency calculation. Cool challenge, but probably not necessary for music.
// ENHANCEMENT: Inaccurate low frequencies — bins below the window duration are unreliable. Dual-FFT?
// ENHANCEMENT: Decimation - reduce the effective sample rate to increase performance.
// ENHANCEMENT: Improve handling of discontinuities (though I have doubt it is possible)
// ENHANCEMENT: Allow decimator frequency to be overridden; include use case like pre-filtered inputs and warn about aliasing if improperly configured
class SpectrumCalculator(
    private val config: SpectrumConfig = ConfigSingleton.spectrum,
    private val audioBuffer: RollingAudioBuffer = RollingAudioBuffer(),
    private val window: Window = config.window.createWindow(),
    private val interpolator: ZeroPaddingInterpolator = ZeroPaddingInterpolator(),
    private val frequencyBinsCalculator: FrequencyBinsCalculator = FftFrequencyBinsCalculator(),
) {

    private val frequencyResolution = 1 / config.frameDuration.inSeconds

    // TODO: Maybe we have a separate bin factory? Decouple FFT from data type?
    data class Result(val bins: FrequencyBins, val pointSpreadFunction: PointSpreadFunction)

    // WARNING: Discontinuous data will cause spectral artifacts
    fun calculate(audio: AudioFrame): Result {
        val preparedFrame = prepareFrame(audio)

        val psf = calculatePsf(preparedFrame)

        val allBins = calculateBins(preparedFrame)
        val validBins = filterBins(allBins, audio.format)


        return Result(validBins, psf)
    }

    val psfCalc = PsfCalculator()

    private fun calculatePsf(preparedFrame: PreparedFrame): PointSpreadFunction {
        val coefficients = window.coefficients(preparedFrame.sampleSize)
        val zeroPadded = interpolator.interpolate(coefficients, preparedFrame.fftLength)
        return psfCalc.calculate(zeroPadded)
    }

    // Frame Prep
    private data class PreparedFrame(
        val audio: AudioFrame,
        val magnitudeCorrectionFactor: Float,
        val sampleSize: Int,
        val fftLength: Int
    )

    private fun prepareFrame(audio: AudioFrame): PreparedFrame {
        val sampleSize = (config.frameDuration.inSeconds * audio.format.sampleRate).toInt()
        val samplesSizeForDesiredSpacing = audio.format.sampleRate / config.approximateBinSpacing
        val optimalFftLength = nextPowerOfTwo(samplesSizeForDesiredSpacing.toInt())

        val preparedAudio = audio
            .let { updateBuffer(it, sampleSize) }
            .let { applyWindowFunction(it) }
            .let { interpolate(it, optimalFftLength) }

        return PreparedFrame(
            audio = preparedAudio,
            magnitudeCorrectionFactor = window.magnitudeCorrectionFactor(sampleSize),
            sampleSize = sampleSize,
            fftLength = optimalFftLength
        )
    }

    private fun updateBuffer(frame: AudioFrame, requiredSize: Int): AudioFrame {
        audioBuffer.size = requiredSize
        return audioBuffer.append(frame)
    }

    private fun applyWindowFunction(audio: AudioFrame): AudioFrame {
        return AudioFrame(
            samples = window.appliedTo(audio.samples),
            format = audio.format
        )
    }

    private fun interpolate(audio: AudioFrame, targetSize: Int): AudioFrame {
        return AudioFrame(
            samples = interpolator.interpolate(audio.samples, targetSize),
            format = audio.format
        )
    }


    // Bin calculation
    private fun calculateBins(preparedFrame: PreparedFrame): FrequencyBins {
        return frequencyBinsCalculator.calculate(
            preparedFrame.audio.samples,
            preparedFrame.audio.format.sampleRate,
            preparedFrame.magnitudeCorrectionFactor
        )
    }

    private fun filterBins(bins: FrequencyBins, format: AudioFormat): FrequencyBins {
        val lowestFrequency = frequencyResolution
        val highestFrequency = format.nyquistFrequency

        return bins.filter { it.frequency in lowestFrequency..highestFrequency }
    }


}