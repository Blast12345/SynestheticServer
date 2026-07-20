package toolkit.monkeyTest

import dsp.filtering.FilterConfig

fun nextHighPassConfig(): FilterConfig.HighPass {
    return FilterConfig.HighPass(nextPositiveFloat(), nextFilterFamily())
}

fun nextLowPassConfig(frequency: Float = nextPositiveFloat()): FilterConfig.LowPass {
    return FilterConfig.LowPass(frequency, nextFilterFamily())
}