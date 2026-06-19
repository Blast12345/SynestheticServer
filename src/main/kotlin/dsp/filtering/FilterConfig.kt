package dsp.filtering

sealed class FilterConfig(val frequency: Float, val family: FilterFamily) {

    class HighPass(frequency: Float, family: FilterFamily) : FilterConfig(frequency, family) {
        override fun frequencyAt(dBFS: Float): Float = frequency / family.rolloffRatio(dBFS)
    }

    class LowPass(frequency: Float, family: FilterFamily) : FilterConfig(frequency, family) {
        override fun frequencyAt(dBFS: Float): Float = frequency * family.rolloffRatio(dBFS)
    }

    abstract fun frequencyAt(dBFS: Float): Float
    
}