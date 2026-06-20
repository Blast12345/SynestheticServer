package lightOrgan.spectralAnalysis.conditioning

import AppConfigSingleton
import audio.samples.AudioFrame
import dsp.Decimator
import dsp.Gain
import dsp.MonoMixer
import dsp.filtering.FilterConfig
import dsp.filtering.Passband
import dsp.filtering.StatefulFilter
import lightOrgan.spectralAnalysis.AudioConditionerConfig

class CachedFactory<K, V>(private val create: (K) -> V) {
    private var key: K? = null
    private var value: V? = null

    fun get(key: K): V {
        if (key != this.key) {
            this.key = key
            value = create(key)
        }
        return value!!
    }
}

class AudioConditioner(
    private val config: () -> AudioConditionerConfig = { AppConfigSingleton.value.spectralAnalysis.audioConditioner },
    private val monoMixer: MonoMixer = MonoMixer(),
    private val gain: Gain = Gain(),
    private val filterFactory: FilterFactory = FilterFactory(),
    private val decimator: Decimator = Decimator(),
) {

    private val highPassFilterCache = CachedFactory<FilterConfig?, StatefulFilter?> { it?.let { filterFactory.create(it) } }
    private val lowPassFilterCache = CachedFactory<FilterConfig?, StatefulFilter?> { it?.let { filterFactory.create(it) } }

    val passband: Passband
        get() {
            val config = this.config()

            return Passband(
                lowerFrequency = config.highPassFilter?.frequencyAt(config.rolloffThreshold) ?: 0f,
                higherFrequency = config.lowPassFilter?.frequencyAt(config.rolloffThreshold) ?: Float.POSITIVE_INFINITY,
            )
        }

    fun condition(audio: AudioFrame): AudioFrame {
        val config = this.config() // Snapshot to prevent settings changes mid-flight
        val highPassFilter = config.highPassFilter?.let { highPassFilterCache.get(it) }
        val lowPassFilter = config.lowPassFilter?.let { lowPassFilterCache.get(it) }

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
        return decimator.decimate(audio, factor)
    }

}