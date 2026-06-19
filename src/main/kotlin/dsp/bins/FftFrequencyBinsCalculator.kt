package dsp.bins

import org.apache.commons.math3.complex.Complex
import org.jtransforms.fft.FloatFFT_1D

class FftFrequencyBinsCalculator : FrequencyBinsCalculator {

    override fun calculate(
        monoSamples: FloatArray,
        sampleRate: Float,
        magnitudeCorrectionFactor: Float
    ): FrequencyBins {
        val fftResult = performFft(monoSamples)

        val binSpacing = sampleRate / monoSamples.size
        val scalingFactor = 2.0 / monoSamples.size

        return fftResult.mapIndexed { index, complex ->
            FrequencyBin(
                frequency = index * binSpacing,
                value = complex.multiply(scalingFactor * magnitudeCorrectionFactor),
            )
        }
    }

    private fun performFft(samples: FloatArray): List<Complex> {
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