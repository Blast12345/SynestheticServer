package dsp.filtering

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