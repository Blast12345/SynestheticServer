package lightOrgan.spectralAnalysis.spectrum

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import audio.samples.RollingAudioBuffer
import config.ConfigSingleton
import dsp.ZeroPaddingInterpolator
import dsp.bins.FrequencyBins
import dsp.bins.FrequencyBinsFactory
import dsp.bins.RealFFT
import dsp.windowing.Window
import extensions.inSeconds
import math.magnitude
import math.nextPowerOfTwo
import org.apache.commons.math3.complex.Complex


data class PointSpreadFunction(
    val values: List<Complex>,
    val centerIndex: Int,
)


// ENHANCEMENT: Multi-resolution bin generations
// ENHANCEMENT: Implement equal-loudness contours (ISO 226:2003). Manual SPL number with future plans of external meter?
// ENHANCEMENT: If implementing other calculation strategies (e.g., DFT, CZT), then create a bin calculator interface
// ENHANCEMENT: Explore sub-frame duration frequency calculation. Cool challenge, but probably not necessary for music.
// ENHANCEMENT: Inaccurate low frequencies — bins below the window duration are unreliable. Dual-FFT?
// ENHANCEMENT: Decimation - reduce the effective sample rate to increase performance.
// ENHANCEMENT: Improve handling of discontinuities (though I have doubt it is possible)
// ENHANCEMENT: Allow decimator frequency to be overridden; include use case like pre-filtered inputs and warn about aliasing if improperly configured
class SpectrumCalculator(
    private val config: SpectralAnalysisConfig = ConfigSingleton.spectralAnalysis,
    private val audioBuffer: RollingAudioBuffer = RollingAudioBuffer(),
    private val window: Window = config.window.createWindow(),
    private val interpolator: ZeroPaddingInterpolator = ZeroPaddingInterpolator(),
    private val realFFT: RealFFT = RealFFT(),
    private val frequencyBinsFactory: FrequencyBinsFactory = FrequencyBinsFactory(),
) {

    private val frequencyResolution = 1 / config.frameDuration.inSeconds

    // TODO: Maybe we have a separate bin factory? Decouple FFT from data type?
    data class Result(val bins: FrequencyBins, val pointSpreadFunction: PointSpreadFunction)

    fun calculate(audio: AudioFrame): Result {
        val sampleSize = (config.frameDuration.inSeconds * audio.format.sampleRate).toInt()
        val samplesSizeForDesiredSpacing = (audio.format.sampleRate / config.approximateBinSpacing).toInt()
        val fftLength = nextPowerOfTwo(samplesSizeForDesiredSpacing)

        // Spectrum
        val bufferedAudio = updateBuffer(audio, sampleSize)
        val windowedSamples = window.appliedTo(bufferedAudio.samples)
        val audioSpectrum = transformToSpectrum(windowedSamples, fftLength)
        val bins = frequencyBinsFactory.create(audioSpectrum, audio.format.sampleRate, fftLength) //, window.magnitudeCorrectionFactor(sampleSize)
        val validBins = filterBins(bins, audio.format)

        // PSF
        val windowCoefficients = window.coefficients(sampleSize)
        val psfSpectrum = transformToSpectrum(windowCoefficients, fftLength)
        val psf = calculatePSF(psfSpectrum)

        return Result(validBins, psf)
    }

    private fun transformToSpectrum(signal: FloatArray, fftLength: Int): List<Complex> {
        val interpolated = interpolator.interpolate(signal, fftLength)
        val normalizationFactor = 2.0 / fftLength * window.magnitudeCorrectionFactor(fftLength)
        return realFFT.forward(interpolated).map { it.multiply(normalizationFactor) }
    }

    private fun calculatePSF(complex: List<Complex>): PointSpreadFunction {
        val peakMagnitude = complex.maxOf { it.magnitude }
        val normalized = complex.map { it.multiply(1.0 / peakMagnitude) }

        val negativeOffsets = normalized.drop(1).reversed().map { it.conjugate() }
        val fullPsf = negativeOffsets + normalized

        return PointSpreadFunction(values = fullPsf, centerIndex = negativeOffsets.size)
    }

    private fun updateBuffer(frame: AudioFrame, requiredSize: Int): AudioFrame {
        audioBuffer.size = requiredSize
        return audioBuffer.append(frame)
    }

    private fun filterBins(bins: FrequencyBins, format: AudioFormat): FrequencyBins {
        val lowestFrequency = frequencyResolution
        val highestFrequency = format.nyquistFrequency

        return bins.filter { it.frequency in lowestFrequency..highestFrequency }
    }


}