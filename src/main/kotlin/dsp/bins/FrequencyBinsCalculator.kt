package dsp.bins

interface FrequencyBinsCalculator {
    fun calculate(
        monoSamples: FloatArray,
        sampleRate: Float
    ): FrequencyBins
}