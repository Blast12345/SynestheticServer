package lightOrgan.spectralAnalysis.conditioning

import audio.samples.AudioFrame
import dsp.Decimator
import dsp.Gain
import dsp.MonoMixer
import dsp.filtering.FilterConfig
import dsp.filtering.StatefulFilter
import lightOrgan.spectralAnalysis.AudioConditionerConfig
import utilities.CachedProvider
import kotlin.math.min

class AudioConditioner(
    private val monoMixer: MonoMixer = MonoMixer(),
    private val gain: Gain = Gain(),
    private val filterFactory: FilterFactory = FilterFactory(),
    private val decimator: Decimator = Decimator(),
) {

    private val highPassFilterCache = CachedProvider<FilterConfig?, StatefulFilter?> { it?.let { filterFactory.create(it) } }
    private val lowPassFilterCache = CachedProvider<FilterConfig?, StatefulFilter?> { it?.let { filterFactory.create(it) } }

    fun condition(audio: AudioFrame, config: AudioConditionerConfig): AudioFrame {
        val highPassFilter = highPassFilterCache.get(config.highPassFilter)
        val lowPassFilter = lowPassFilterCache.get(config.lowPassFilter)

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
            val targetNyquist = min(config.passband.higherFrequency, conditionedAudio.format.sampleRate / 2f) // TODO: Test me
            conditionedAudio = decimate(conditionedAudio, targetNyquist)
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
        return decimator.decimate(audio, factor)
    }

}