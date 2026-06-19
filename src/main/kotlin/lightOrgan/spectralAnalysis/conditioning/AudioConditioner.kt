package lightOrgan.spectralAnalysis.conditioning

import audio.samples.AudioFrame
import dsp.Decimator
import dsp.Gain
import dsp.MonoMixer
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.spectrum.FilterManager

data class Passband(
    val lowerFrequency: Float?,
    val higherFrequency: Float?,
) {
    operator fun contains(frequency: Float): Boolean {
        val aboveLower = lowerFrequency?.let { frequency >= it } ?: true
        val belowUpper = higherFrequency?.let { frequency <= it } ?: true
        return aboveLower && belowUpper
    }
}

// TODO: Test me
class AudioConditioner(
    private val config: SpectralAnalysisConfig,
    private val monoMixer: MonoMixer = MonoMixer(),
    private val gain: Gain = Gain(),
    private val filterManager: FilterManager = FilterManager(config.highPassFilter, config.lowPassFilter),
    private val decimator: Decimator = Decimator(),
) {

    // high-pass removes lows  → its rolloff is the LOWER edge
    // low-pass  removes highs → its rolloff is the UPPER edge
    val passband: Passband
        get() = Passband(
            lowerFrequency = filterManager.highPassConfig?.frequencyAt(config.rolloffThreshold),
            higherFrequency = filterManager.lowPassConfig?.frequencyAt(config.rolloffThreshold),
        )

    fun condition(audio: AudioFrame): AudioFrame {
        val targetNyquist = passband.higherFrequency ?: audio.format.nyquistFrequency

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

}