package lightOrgan.spectralAnalysis.noiseReduction

import config.AppConfigSingleton
import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks
import extensions.times
import lightOrgan.spectralAnalysis.NoiseReductionConfig
import org.apache.commons.math3.complex.Complex

// TODO: Test me
class SpectralGate(
    private val config: () -> NoiseReductionConfig = { AppConfigSingleton.value.spectralAnalysis.noiseReduction },
) : NoiseReducer {

    override fun reduceSpectrum(spectrum: FrequencyBins): FrequencyBins {
        val config = this.config()

        if (config.threshold == 0.0) return spectrum

        return spectrum.map { bin ->
            val corrected = correctMagnitude(bin.magnitude.toDouble(), config.threshold, config.kneeWidth)
            if (corrected == 0.0) bin.copy(value = Complex.ZERO)
            else bin.copy(value = bin.value * (corrected / bin.magnitude))
        }
    }

    override fun reducePeaks(peaks: SpectralPeaks): SpectralPeaks {
        val config = this.config()

        if (config.threshold == 0.0) return peaks

        return peaks.mapNotNull { peak ->
            val corrected = correctMagnitude(peak.magnitude.toDouble(), config.threshold, config.kneeWidth)
            if (corrected == 0.0) null
            else peak.copy(magnitude = corrected.toFloat())
        }
    }


    private fun correctMagnitude(magnitude: Double, threshold: Double, kneeWidth: Double): Double {
        if (magnitude <= threshold) return 0.0

        val recovery = 1.0 / (1.0 - threshold)

        val kneeEnd = threshold + kneeWidth
        if (magnitude >= kneeEnd) return (magnitude - threshold) * recovery
        val t = (magnitude - threshold) / kneeWidth  // 0..1 within knee
        return t * t * kneeWidth * recovery  // quadratic ramp
    }

}