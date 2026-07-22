package lightOrgan.spectralAnalysis.conditioning

import audio.samples.AudioFrame
import dsp.Decimator
import dsp.Gain
import dsp.MonoMixer
import dsp.filtering.FilterConfig
import dsp.filtering.StatefulFilter
import utilities.CachedProvider

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
            conditionedAudio = gain.apply(conditionedAudio, config.gainDb)
        }

        if (highPassFilter != null) {
            conditionedAudio = highPassFilter.filter(conditionedAudio)
        }

        if (lowPassFilter != null) {
            conditionedAudio = lowPassFilter.filter(conditionedAudio)
        }

        if (config.decimationFrequency != null &&
            config.decimationFrequency < conditionedAudio.format.nyquistFrequency
        ) {
            conditionedAudio = decimate(conditionedAudio, config.decimationFrequency)
        }

        return conditionedAudio
    }

    private fun Gain.apply(audioFrame: AudioFrame, gainDb: Float): AudioFrame {
        val adjustedSamples = apply(audioFrame.samples, gainDb)
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