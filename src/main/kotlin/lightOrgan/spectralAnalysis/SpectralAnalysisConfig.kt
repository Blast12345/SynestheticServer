package lightOrgan.spectralAnalysis

import dsp.filtering.FilterConfig
import dsp.filtering.Passband
import dsp.windowing.WindowType
import extensions.inSeconds
import lightOrgan.spectralAnalysis.noiseReduction.NoiseReducer
import lightOrgan.spectralAnalysis.peaks.PeakExtractorConfig
import kotlin.time.Duration

data class SpectralAnalysisConfig(
    val audioConditioner: AudioConditionerConfig,
    val spectrumCalculator: SpectrumCalculatorConfig,
    val peakExtractor: PeakExtractorConfig,
    val noiseReduction: NoiseReducer.Config?,
)

data class AudioConditionerConfig(
    val gainDb: Float,
    val rolloffThreshold: Float?, // e.g. -48 dBFS
    val highPassFilter: FilterConfig.HighPass?,
    val lowPassFilter: FilterConfig.LowPass?,
    val decimate: Boolean,
) {

    // TODO: Test me?
    val passband: Passband
        get() {
            val threshold = rolloffThreshold ?: return Passband.ALL

            return Passband(
                lowerFrequency = highPassFilter?.frequencyAt(threshold) ?: 0f,
                higherFrequency = lowPassFilter?.frequencyAt(threshold) ?: Float.POSITIVE_INFINITY,
            )
        }


    // Passband
//    @Test
//    fun `given a high pass filter, passband lower frequency is the rolloff`() {
//        val sut = createSUT()
//
//        assertEquals(
//            highPassConfig.frequencyAt(minimalConfig.rolloffThreshold, minimalConfig.copy(highPassFilter = highPassConfig)),
//            sut.passband.lowerFrequency,
//        )
//    }
//
//    @Test
//    fun `given no high pass filter, passband lower frequency is zero`() {
//        val sut = createSUT(minimalConfig)
//        assertEquals(0f, sut.passband.lowerFrequency)
//    }
//
//    @Test
//    fun `given a low pass filter, passband upper frequency is the rolloff`() {
//        val sut = createSUT(minimalConfig.copy(lowPassFilter = lowPassConfig))
//
//        assertEquals(
//            lowPassConfig.frequencyAt(minimalConfig.rolloffThreshold),
//            sut.passband.higherFrequency
//        )
//    }
//
//    @Test
//    fun `given no low pass filter, passband upper frequency is infinity`() {
//        val sut = createSUT(minimalConfig)
//        assertEquals(Float.POSITIVE_INFINITY, sut.passband.higherFrequency)
//    }

}

data class SpectrumCalculatorConfig(
    val frameDuration: Duration,
    val approximateBinSpacing: Float,
    val window: WindowType,
) {

    val frequencyResolution = 1 / frameDuration.inSeconds

}