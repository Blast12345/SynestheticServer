package lightOrgan.spectrum

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import audio.samples.RollingAudioBuffer
import config.ConfigSingleton
import dsp.Decimator
import dsp.Gain
import dsp.MonoMixer
import dsp.ZeroPaddingInterpolator
import dsp.bins.FftFrequencyBinsCalculator
import dsp.bins.FrequencyBins
import dsp.bins.FrequencyBinsCalculator
import dsp.peakExtraction.ParabolicSpectralPeakExtractor
import dsp.peakExtraction.SpectralPeakExtractor
import dsp.windowing.Window
import extensions.inSeconds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import math.nextPowerOfTwo

// ENHANCEMENT: Multi-resolution bin generations
// ENHANCEMENT: Implement equal-loudness contours (ISO 226:2003). Manual SPL number with future plans of external meter?
// ENHANCEMENT: If implementing other calculation strategies (e.g., DFT, CZT), then create a bin calculator interface
// ENHANCEMENT: Explore sub-frame duration frequency calculation. Cool challenge, but probably not necessary for music.
// ENHANCEMENT: Inaccurate low frequencies — bins below the window duration are unreliable. Dual-FFT?
// ENHANCEMENT: Decimation - reduce the effective sample rate to increase performance.
// ENHANCEMENT: Improve handling of discontinuities (though I have doubt it is possible)
// ENHANCEMENT: Allow decimator frequency to be overridden; include use case like pre-filtered inputs and warn about aliasing if improperly configured
class SpectrumManager(
    private val config: SpectrumConfig = ConfigSingleton.spectrum,
    private val monoMixer: MonoMixer = MonoMixer(),
    private val gain: Gain = Gain(),
    private val filterManager: FilterManager = FilterManager(config.highPassFilter, config.lowPassFilter),
    private val decimator: Decimator = Decimator(),
    private val audioBuffer: RollingAudioBuffer = RollingAudioBuffer(),
    private val window: Window = config.window.createWindow(),
    private val interpolator: ZeroPaddingInterpolator = ZeroPaddingInterpolator(),
    private val frequencyBinsCalculator: FrequencyBinsCalculator = FftFrequencyBinsCalculator(),
) {
    private var peakExtractor: SpectralPeakExtractor = ParabolicSpectralPeakExtractor()

    private val _spectralAnalysis = MutableStateFlow(SpectralAnalysis.EMPTY)
    val spectralAnalysis: StateFlow<SpectralAnalysis> = _spectralAnalysis.asStateFlow()

    // WARNING: Discontinuous data will cause spectral artifacts
    fun calculate(audio: AudioFrame): SpectralAnalysis {
        val conditionedAudio = conditionAudio(audio)
        val preparedFrame = prepareFrame(conditionedAudio)
        val allBins = calculateBins(preparedFrame)
        val relevantBins = filterBins(allBins, preparedFrame.audio.format)
        val peaks = peakExtractor.extract(relevantBins) // TODO:

        val spectralAnalysis = SpectralAnalysis(relevantBins, peaks)

        _spectralAnalysis.value = spectralAnalysis
        return spectralAnalysis
    }

    // Conditioning
    private fun conditionAudio(audio: AudioFrame): AudioFrame {
        val highStopbandFrequency = filterManager.lowPassConfig?.frequencyAt(config.rolloffThreshold)
        val targetNyquist = highStopbandFrequency ?: audio.format.nyquistFrequency

        return audio
            .let { monoMixer.mix(it) }
            .let { applyGain(it, config.gainDb) }
            .let { filterManager.filter(it) }
            .let { decimateIfNeeded(it, targetNyquist) }
    }

    private fun applyGain(audioFrame: AudioFrame, gainDb: Float): AudioFrame {
        val adjustedSamples = gain.apply(audioFrame.samples, gainDb)
        return audioFrame.copy(samples = adjustedSamples)
    }

    private fun decimateIfNeeded(audio: AudioFrame, targetNyquist: Float): AudioFrame {
        val factor = decimator.decimationFactor(audio.format.sampleRate, targetNyquist)
        val effectiveSampleRate = audio.format.sampleRate / factor

        if (factor <= 1) return audio

        return AudioFrame(
            samples = decimator.decimate(audio.samples, factor, audio.format.sampleRate, audio.format.channels),
            format = audio.format.copy(sampleRate = effectiveSampleRate)
        )
    }

    // Frame Prep
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
            magnitudeCorrectionFactor = window.magnitudeCorrectionFactor(sampleSize)
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

    private data class PreparedFrame(
        val audio: AudioFrame,
        val magnitudeCorrectionFactor: Float
    )

    // Bin calculation
    private fun calculateBins(preparedFrame: PreparedFrame): FrequencyBins {
        return frequencyBinsCalculator.calculate(
            preparedFrame.audio.samples,
            preparedFrame.audio.format.sampleRate,
            preparedFrame.magnitudeCorrectionFactor
        )
    }

    // Filtering
    private fun filterBins(bins: FrequencyBins, format: AudioFormat): FrequencyBins {
        val frequencyResolution = 1 / config.frameDuration.inSeconds
        val nyquist = format.nyquistFrequency
        val lowStopbandFrequency = filterManager.highPassConfig?.frequencyAt(config.rolloffThreshold)
        val highStopbandFrequency = filterManager.lowPassConfig?.frequencyAt(config.rolloffThreshold)

        val lowestFrequency = maxOf(frequencyResolution, lowStopbandFrequency ?: frequencyResolution)
        val highestFrequency = minOf(nyquist, highStopbandFrequency ?: nyquist)

        return bins.filter { it.frequency in lowestFrequency..highestFrequency }
    }

}