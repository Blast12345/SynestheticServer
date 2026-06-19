package dsp.filtering

import dsp.filtering.cascaded.butterworth.ButterworthHighPass
import dsp.filtering.cascaded.butterworth.ButterworthLowPass

class FilterBuilder {

    fun build(config: FilterConfig, sampleRate: Float): Filter {
        return when (config.family) {
            is FilterFamily.Butterworth -> buildButterworth(config, config.family, sampleRate)
        }
    }

    private fun buildButterworth(
        config: FilterConfig,
        family: FilterFamily.Butterworth,
        sampleRate: Float
    ): Filter {
        val order = family.order.value
        return when (config) {
            is FilterConfig.LowPass -> ButterworthLowPass(config.frequency, order, sampleRate)
            is FilterConfig.HighPass -> ButterworthHighPass(config.frequency, order, sampleRate)
        }
    }

}