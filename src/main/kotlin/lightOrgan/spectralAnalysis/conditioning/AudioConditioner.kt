package lightOrgan.spectralAnalysis.conditioning

import audio.samples.AudioFrame
import dsp.Decimator
import dsp.Gain
import dsp.MonoMixer
import lightOrgan.spectralAnalysis.SpectralAnalysisConfig
import lightOrgan.spectralAnalysis.spectrum.FilterFactory
import lightOrgan.spectralAnalysis.spectrum.StatefulFilter

// TODO: Move me
data class Passband(
    val lowerFrequency: Float,
    val higherFrequency: Float,
) {

    operator fun contains(frequency: Float): Boolean {
        return frequency in lowerFrequency..higherFrequency
    }

}

class AudioConditioner(
    private val config: SpectralAnalysisConfig,
    private val monoMixer: MonoMixer = MonoMixer(),
    private val gain: Gain = Gain(),
    private val filterFactory: FilterFactory = FilterFactory(),
    private val decimator: Decimator = Decimator(),
) {

    private val highPassFilter: StatefulFilter? = config.highPassFilter?.let { filterFactory.create(it) }
    private val lowPassFilter: StatefulFilter? = config.lowPassFilter?.let { filterFactory.create(it) }

    val passband: Passband
        get() = Passband(
            lowerFrequency = highPassFilter?.frequencyAt(config.rolloffThreshold) ?: 0f,
            higherFrequency = lowPassFilter?.frequencyAt(config.rolloffThreshold) ?: Float.POSITIVE_INFINITY,
        )

    fun condition(audio: AudioFrame): AudioFrame {
        var conditionedAudio = audio

        if (conditionedAudio.format.channels > 1) {
            conditionedAudio = monoMixer.mix(conditionedAudio)
        }

        if (config.gainDb != 0f) {
            conditionedAudio = applyGain(conditionedAudio, config.gainDb)
        }

        if (highPassFilter != null) {
            conditionedAudio = highPassFilter.filter(conditionedAudio)
        }

        if (lowPassFilter != null) {
            conditionedAudio = lowPassFilter.filter(conditionedAudio)
        }

        if (config.decimate && lowPassFilter != null) {
            conditionedAudio = decimate(conditionedAudio, passband.higherFrequency)
        }

        return conditionedAudio
    }

    private fun applyGain(audioFrame: AudioFrame, gainDb: Float): AudioFrame {
        val adjustedSamples = gain.apply(audioFrame.samples, gainDb)
        return audioFrame.copy(samples = adjustedSamples)
    }

    private fun StatefulFilter.filter(audioFrame: AudioFrame): AudioFrame {
        val filteredSamples = filter(audioFrame.samples, audioFrame.format.sampleRate)
        return audioFrame.copy(samples = filteredSamples)
    }

    private fun decimate(audio: AudioFrame, targetNyquistFrequency: Float): AudioFrame {
        val factor = decimator.decimationFactor(audio.format.sampleRate, targetNyquistFrequency)
        val effectiveSampleRate = audio.format.sampleRate / factor

        return AudioFrame(
            samples = decimator.decimate(audio.samples, factor, audio.format.sampleRate, audio.format.channels),
            format = audio.format.copy(sampleRate = effectiveSampleRate)
        )
    }

}