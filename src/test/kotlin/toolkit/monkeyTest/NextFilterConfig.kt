package toolkit.monkeyTest

import dsp.filtering.FilterConfig

fun nextHighPassConfig(): FilterConfig.HighPass {
    return FilterConfig.HighPass(nextPositiveFloat(), nextFilterFamily())
}

fun nextLowPassConfig(): FilterConfig.LowPass {
    return FilterConfig.LowPass(nextPositiveFloat(), nextFilterFamily())
}