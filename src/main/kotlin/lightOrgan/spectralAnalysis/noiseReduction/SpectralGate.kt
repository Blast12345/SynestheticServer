package lightOrgan.spectralAnalysis.noiseReduction

import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks
import extensions.times
import org.apache.commons.math3.complex.Complex
import kotlin.math.pow

data class SpectralGateConfig(
    val thresholdDb: Double
) : NoiseReducer.Config

// TODO: Test me
class SpectralGate(
    val config: SpectralGateConfig
) : NoiseReducer {

    override fun reduceSpectrum(spectrum: FrequencyBins): FrequencyBins {
        return spectrum.map { bin ->
            val gain = computeGain(bin.magnitude.toDouble())
            if (gain == 0.0) bin.copy(value = Complex.ZERO)
            else bin.copy(value = bin.value * gain)
        }
    }

    override fun reducePeaks(peaks: SpectralPeaks): SpectralPeaks {
        return peaks.mapNotNull { peak ->
            val gain = computeGain(peak.magnitude.toDouble())
            if (gain == 0.0) null
            else peak.copy(magnitude = (peak.magnitude * gain).toFloat())
        }
    }

    private fun computeGain(magnitude: Double): Double {
        if (magnitude <= 0.0) return 0.0

        val threshold = 10.0.pow(config.thresholdDb / 20.0)
        if (magnitude <= threshold) return 0.0

        val recovery = 1.0 / (1.0 - threshold)
        return (magnitude - threshold) * recovery / magnitude
    }

}