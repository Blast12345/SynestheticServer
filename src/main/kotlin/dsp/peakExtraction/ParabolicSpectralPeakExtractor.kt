package dsp.peakExtraction

import dsp.bins.FrequencyBins
import kotlin.math.log10
import kotlin.math.pow

class ParabolicSpectralPeakExtractor : SpectralPeakExtractor {

    fun extract(spectrum: FrequencyBins): SpectralPeaks {
        return spectrum
            .map { bin -> LogBin(bin.frequency, safeLog10(bin.magnitude)) }
            .windowed(size = 3, step = 1)
            .mapNotNull { (previous, current, next) ->
                if (containsPeak(previous, current, next)) {
                    interpolate(previous, current, next)
                } else {
                    null
                }
            }
    }

    private fun containsPeak(previous: LogBin, current: LogBin, next: LogBin): Boolean {
        // Peak detection is biased to one side to avoid duplicates when plateaus are found
        // e.g. magnitudes [0.1, 0.5, 0.5, 0.1] should result in one peak between the 0.5 values
        return current.logMagnitude > previous.logMagnitude && current.logMagnitude >= next.logMagnitude
    }

    private fun interpolate(previous: LogBin, current: LogBin, next: LogBin): SpectralPeak {
        val slope = previous.logMagnitude - next.logMagnitude
        val curvature = previous.logMagnitude - 2 * current.logMagnitude + next.logMagnitude
        check(curvature < 0) {
            "Curvature must be negative (concave down); isPeak should guarantee this"
        }

        val delta = 0.5f * slope / curvature
        val binWidth = next.frequency - current.frequency
        val interpolatedLogMagnitude = current.logMagnitude - 0.25f * slope * delta

        return SpectralPeak(
            frequency = current.frequency + delta * binWidth,
            magnitude = 10f.pow(interpolatedLogMagnitude)
        )
    }

    private data class LogBin(val frequency: Float, val logMagnitude: Float)

    private fun safeLog10(value: Float): Float = log10(value.coerceAtLeast(Float.MIN_VALUE))

}