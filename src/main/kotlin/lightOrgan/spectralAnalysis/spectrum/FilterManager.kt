package lightOrgan.spectralAnalysis.spectrum

import annotations.SkipCoverage
import dsp.filtering.Filter
import dsp.filtering.FilterBuilder
import dsp.filtering.FilterConfig
import dsp.filtering.FilterType

// TODO: Test me and move me
class StatefulFilter(
    private val config: FilterConfig,
    private val filterBuilder: FilterBuilder = FilterBuilder(),
) {

    private var filter: Filter? = null
    private var sampleRate: Float? = null

    fun filter(samples: FloatArray, sampleRate: Float): FloatArray {
        if (sampleRate != this.sampleRate) {
            this.sampleRate = sampleRate
            filter = filterBuilder.build(config, sampleRate)
        }
        return filter!!.filter(samples)
    }

    fun frequencyAt(dBFS: Float): Float = config.frequencyAt(dBFS)

}

@SkipCoverage
class FilterFactory {

    fun create(config: FilterConfig): StatefulFilter {
        return StatefulFilter(config)
    }

}

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

    fun filter(samples: FloatArray, sampleRate: Float): FloatArray {
        rebuildIfNeeded(sampleRate)

        var filteredSamples = samples

        highPassFilter?.let { filteredSamples = it.filter(filteredSamples) }
        lowPassFilter?.let { filteredSamples = it.filter(filteredSamples) }

        return filteredSamples
    }

    private fun rebuildIfNeeded(sampleRate: Float) {
        if (sampleRate == this.sampleRate) return

        this.sampleRate = sampleRate
        highPassFilter = highPassConfig?.let { filterBuilder.build(it, sampleRate) }
        lowPassFilter = lowPassConfig?.let { filterBuilder.build(it, sampleRate) }
    }

}