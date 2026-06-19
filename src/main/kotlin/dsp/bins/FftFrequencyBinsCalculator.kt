package dsp.bins

import org.apache.commons.math3.complex.Complex
import org.jtransforms.fft.FloatFFT_1D

class FftFrequencyBinsCalculator : FrequencyBinsCalculator {

    override fun calculate(
        monoSamples: FloatArray,
        sampleRate: Float
    ): FrequencyBins {
        val fftResult = performFft(monoSamples)

        val binCount = monoSamples.size / 2
        val binSpacing = sampleRate / monoSamples.size

        return fftResult.mapIndexed { index, complex ->
            // Energy is split between both sides of the FFT, so single-sided bins are doubled.
            // DC and Nyquist have no conjugate mirror, so they are not doubled.
            val hasConjugate = index != 0 && index != binCount

            val scalingFactor = if (hasConjugate) {
                2.0 / monoSamples.size
            } else {
                1.0 / monoSamples.size
            }

            FrequencyBin(
                frequency = index * binSpacing,
                value = complex.multiply(scalingFactor),
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