package dsp.peakExtraction

import dsp.bins.FrequencyBins
import dsp.clean.InterpolatedCleanAlgorithm1D
import dsp.clean.InterpolatedCleanComponent1D
import extensions.plus
import org.apache.commons.math3.complex.Complex
import org.jtransforms.fft.FloatFFT_1D
import kotlin.math.floor

class CleanPeakExtractor(
    val cleanAlgorithm: InterpolatedCleanAlgorithm1D = InterpolatedCleanAlgorithm1D(),
) : SpectralPeakExtractor {

    fun extract(spectrum: FrequencyBins, pointSpreadFunction: PointSpreadFunction): SpectralPeaks {
        val dirtySignal = spectrum.map { it.value }

        val result = cleanAlgorithm.clean(
            dirtySignal,
            componentResponse = { component ->
                val response = List(dirtySignal.size) { index ->
                    val pointSpreadFunctionPosition = index - component.position + pointSpreadFunction.centerIndex
                    sampleInterpolated(pointSpreadFunction.values, pointSpreadFunctionPosition).multiply(component.value)
                }
                response
            },
            loopGain = 0.1,
            maxIterations = 200,
            magnitudeThreshold = 0.01
        )

        val binSpacing = spectrum[1].frequency - spectrum[0].frequency

        return consolidate(result.components, binSpacing)
            .map { (position, value) ->
                SpectralPeak(
                    frequency = (spectrum[0].frequency + position * binSpacing).toFloat(),
                    magnitude = value.abs().toFloat()
                )
            }
    }

    private fun consolidate(
        components: List<InterpolatedCleanComponent1D>,
        binSpacing: Float,
        toleranceBins: Double = 1.0
    ): List<Pair<Double, Complex>> {
        if (components.isEmpty()) return emptyList()

        val sorted = components.sortedBy { it.position }
        val groups = mutableListOf<MutableList<InterpolatedCleanComponent1D>>()

        for (component in sorted) {
            val lastGroup = groups.lastOrNull()
            if (lastGroup != null && (component.position - lastGroup.last().position) <= toleranceBins) {
                lastGroup.add(component)
            } else {
                groups.add(mutableListOf(component))
            }
        }

        return groups.map { group ->
            val totalValue = group.fold(Complex(0.0, 0.0)) { sum, c -> sum + c.value }
            val weightedPosition = group.sumOf { it.position * it.value.abs() } / group.sumOf { it.value.abs() }
            Pair(weightedPosition, totalValue)
        }
    }

    private fun sampleInterpolated(values: List<Complex>, position: Double): Complex {
        val lower = floor(position).toInt()
        val upper = lower + 1
        if (upper < 0 || lower >= values.size) return Complex(0.0, 0.0)
        val fraction = position - lower
        val lowerValue = if (lower < 0) Complex(0.0, 0.0) else values[lower]
        val upperValue = if (upper >= values.size) Complex(0.0, 0.0) else values[upper]
        return lowerValue.multiply(1.0 - fraction).add(upperValue.multiply(fraction))
    }

}

data class PointSpreadFunction(
    val values: List<Complex>,
    val centerIndex: Int,
)

// realForward
class PsfCalculator {

    fun calculate(window: FloatArray): PointSpreadFunction {
        val positiveSpectrum = realForwardFft(window)
        val peakMagnitude = positiveSpectrum.maxOf { it.abs() }
        val normalized = positiveSpectrum.map { it.multiply(1.0 / peakMagnitude) }

        val negativeOffsets = normalized.drop(1).reversed().map { it.conjugate() }
        val fullPsf = negativeOffsets + normalized

        return PointSpreadFunction(values = fullPsf, centerIndex = negativeOffsets.size)
    }

    private fun realForwardFft(samples: FloatArray): List<Complex> {
        val packed = samples.copyOf()
        FloatFFT_1D(packed.size.toLong()).realForward(packed)
        return unpack(packed)
    }

    private fun unpack(packed: FloatArray): List<Complex> {
        val binCount = packed.size / 2
        return (0..binCount).map { index ->
            when (index) {
                0 -> Complex(packed[0].toDouble(), 0.0)
                binCount -> Complex(packed[1].toDouble(), 0.0)
                else -> Complex(
                    packed[2 * index].toDouble(),
                    packed[2 * index + 1].toDouble()
                )
            }
        }
    }
}
// complexForward
//class PsfCalculator {
//
//    fun calculate(window: FloatArray): PointSpreadFunction {
//        val spectrum = fullComplexFft(window)
//        val shifted = fftShift(spectrum)
//        val peakMagnitude = shifted.maxOf { it.abs() }
//        val normalized = shifted.map { it.multiply(1.0 / peakMagnitude) }
//        return PointSpreadFunction(values = normalized, centerIndex = window.size / 2)
//    }
//
//
//    private fun fullComplexFft(window: FloatArray): List<Complex> {
//        val interleaved = FloatArray(window.size * 2)
//        window.forEachIndexed { index, sample -> interleaved[2 * index] = sample }
//        FloatFFT_1D(window.size.toLong()).complexForward(interleaved)
//        return window.indices.map { index ->
//            Complex(interleaved[2 * index].toDouble(), interleaved[2 * index + 1].toDouble())
//        }
//    }
//
//    private fun fftShift(spectrum: List<Complex>): List<Complex> {
//        val splitPoint = (spectrum.size + 1) / 2
//        return spectrum.subList(splitPoint, spectrum.size) + spectrum.subList(0, splitPoint)
//    }
//}