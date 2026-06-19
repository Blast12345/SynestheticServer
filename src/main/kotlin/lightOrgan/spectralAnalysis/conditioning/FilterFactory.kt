package lightOrgan.spectralAnalysis.conditioning

import annotations.SkipCoverage
import dsp.filtering.FilterConfig
import dsp.filtering.StatefulFilter

@SkipCoverage
class FilterFactory {

    fun create(config: FilterConfig): StatefulFilter {
        return StatefulFilter(config)
    }

}