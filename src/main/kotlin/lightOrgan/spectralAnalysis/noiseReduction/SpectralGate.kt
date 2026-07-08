package lightOrgan.spectralAnalysis.noiseReduction

import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks
import extensions.times
import lightOrgan.spectralAnalysis.NoiseReductionConfig
import org.apache.commons.math3.complex.Complex
import kotlin.math.pow

// TODO: Test me
class SpectralGate : NoiseReducer {

    override fun reduceSpectrum(spectrum: FrequencyBins, config: NoiseReductionConfig): FrequencyBins {
        return spectrum.map { bin ->
            val gain = computeGain(bin.magnitude.toDouble(), config.thresholdDb, config.kneeWidthDb)
            if (gain == 0.0) bin.copy(value = Complex.ZERO)
            else bin.copy(value = bin.value * gain)
        }
    }

    override fun reducePeaks(peaks: SpectralPeaks, config: NoiseReductionConfig): SpectralPeaks {
        return peaks.mapNotNull { peak ->
            val gain = computeGain(peak.magnitude.toDouble(), config.thresholdDb, config.kneeWidthDb)
            if (gain == 0.0) null
            else peak.copy(magnitude = (peak.magnitude * gain).toFloat())
        }
    }

    private fun computeGain(magnitude: Double, thresholdDb: Double, kneeWidthDb: Double): Double {
        if (magnitude <= 0.0) return 0.0

        val threshold = 10.0.pow(thresholdDb / 20.0)

        if (magnitude <= threshold) return 0.0

        val kneeEnd = 10.0.pow((thresholdDb + kneeWidthDb) / 20.0)
        val recovery = 1.0 / (1.0 - threshold)

        if (magnitude >= kneeEnd) return (magnitude - threshold) * recovery / magnitude

        val t = (magnitude - threshold) / (kneeEnd - threshold)
        return t * t * (kneeEnd - threshold) * recovery / magnitude
    }

}