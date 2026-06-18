package lightOrgan.spectralAnalysis.spectrum

import audio.samples.AudioFrame
import dsp.filtering.Filter
import dsp.filtering.FilterBuilder
import dsp.filtering.FilterConfig
import dsp.filtering.FilterType

// ENHANCEMENT: Show the filter response in the UI
// ENHANCEMENT: Make configs configurable via the UI, then automatically rebuild filters
class FilterManager(
    val highPassConfig: FilterConfig?,
    val lowPassConfig: FilterConfig?,
    private val filterBuilder: FilterBuilder = FilterBuilder(),
) {

    init {
        require(highPassConfig == null || highPassConfig.type is FilterType.HighPass) {
            "highPassConfig must be a high-pass filter"
        }
        require(lowPassConfig == null || lowPassConfig.type is FilterType.LowPass) {
            "lowPassConfig must be a low-pass filter"
        }
    }

    private var highPassFilter: Filter? = null
    private var lowPassFilter: Filter? = null
    private var sampleRate: Float? = null

    fun filter(audio: AudioFrame): AudioFrame {
        rebuildIfNeeded(audio.format.sampleRate)

        var samples = audio.samples

        highPassFilter?.let { samples = it.filter(samples) }
        lowPassFilter?.let { samples = it.filter(samples) }

        return AudioFrame(samples, audio.format)
    }

    private fun rebuildIfNeeded(sampleRate: Float) {
        if (sampleRate == this.sampleRate) return

        this.sampleRate = sampleRate
        highPassFilter = highPassConfig?.let { filterBuilder.build(it, sampleRate) }
        lowPassFilter = lowPassConfig?.let { filterBuilder.build(it, sampleRate) }
    }

}