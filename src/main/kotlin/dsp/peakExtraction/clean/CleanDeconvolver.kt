//package dsp.peakExtraction.clean
//
//import dsp.bins.FrequencyBin
//import dsp.bins.FrequencyBins
//import org.jtransforms.fft.FloatFFT_1D
//import kotlin.math.*
//
//interface PointSpreadFunction {
//    fun realAt(binOffset: Float): Float
//    fun imaginaryAt(binOffset: Float): Float
//}
//
//class CleanDeconvolver(
//    private val loopGain: Float = 0.1f,
//    private val maxIterations: Int = 200,
//    private val dynamicRangeDb: Float = 40f,
//) {
//
//    fun deconvolve(spectrum: FrequencyBins, psf: PointSpreadFunction): FrequencyBins {
//        val residualReal = spectrum.map { it.value.real }
//        val residualImaginary = spectrum.map { it.value.imaginary }
//
//        val components = mutableListOf<FrequencyBin>()
//        val peakMagnitude = (0 until spectrum.size).maxOf { spectrum.magnitudeAt(it) }
//        val noiseFloor = peakMagnitude * 10f.pow(-dynamicRangeDb / 20f)
//
//        for (iteration in 0 until maxIterations) {
//            val peak = findStrongestPeak(residualReal, residualImaginary, spectrum.binSpacing)
//                ?: break
//            if (peak.magnitude < noiseFloor) break
//
//            val phase = peakPhase(residualReal, residualImaginary, peak.frequency, spectrum.binSpacing)
//            val contribution = peak.magnitude * loopGain
//            val contributionReal = contribution * cos(phase)
//            val contributionImaginary = contribution * sin(phase)
//
//            components.add(FrequencyBin(peak.frequency, contribution))
//
//            subtractPsf(
//                residualReal, residualImaginary,
//                peak.frequency, contributionReal, contributionImaginary,
//                spectrum.binSpacing, spectrum.size, psf,
//            )
//        }
//
//        return mergeComponents(components, spectrum.binSpacing)
//    }
//
//    private fun findStrongestPeak(
//        real: FloatArray,
//        imaginary: FloatArray,
//        binSpacing: Float,
//    ): FrequencyBin? {
//        if (real.isEmpty()) return null
//
//        val peakIndex = (real.indices).maxByOrNull { magnitudeAt(real, imaginary, it) }
//            ?: return null
//
//        val peakMagnitude = magnitudeAt(real, imaginary, peakIndex)
//        if (peakMagnitude <= 0f) return null
//
//        if (peakIndex == 0 || peakIndex == real.lastIndex) {
//            return FrequencyBin(peakIndex * binSpacing, peakMagnitude)
//        }
//
//        return interpolatePeak(
//            previous = FrequencyBin((peakIndex - 1) * binSpacing, magnitudeAt(real, imaginary, peakIndex - 1)),
//            current = FrequencyBin(peakIndex * binSpacing, peakMagnitude),
//            next = FrequencyBin((peakIndex + 1) * binSpacing, magnitudeAt(real, imaginary, peakIndex + 1)),
//        )
//    }
//
//    private fun peakPhase(
//        real: FloatArray,
//        imaginary: FloatArray,
//        peakFrequency: Float,
//        binSpacing: Float,
//    ): Float {
//        val nearestIndex = (peakFrequency / binSpacing).roundToInt().coerceIn(real.indices)
//        return atan2(imaginary[nearestIndex], real[nearestIndex])
//    }
//
//    private fun subtractPsf(
//        residualReal: FloatArray,
//        residualImaginary: FloatArray,
//        peakFrequency: Float,
//        componentReal: Float,
//        componentImaginary: Float,
//        binSpacing: Float,
//        spectrumSize: Int,
//        psf: PointSpreadFunction,
//    ) {
//        for (index in 0 until spectrumSize) {
//            val offset = (index * binSpacing - peakFrequency) / binSpacing
//            val psfReal = psf.realAt(offset)
//            val psfImaginary = psf.imaginaryAt(offset)
//
//            residualReal[index] -= componentReal * psfReal - componentImaginary * psfImaginary
//            residualImaginary[index] -= componentReal * psfImaginary + componentImaginary * psfReal
//        }
//    }
//
//    private fun magnitudeAt(real: FloatArray, imaginary: FloatArray, index: Int): Float {
//        val r = real[index]
//        val i = imaginary[index]
//        return sqrt(r * r + i * i)
//    }
//
//    private fun interpolatePeak(
//        previous: FrequencyBin,
//        current: FrequencyBin,
//        next: FrequencyBin,
//    ): FrequencyBin {
//        val alpha = log10(previous.magnitude.coerceAtLeast(Float.MIN_VALUE))
//        val beta = log10(current.magnitude.coerceAtLeast(Float.MIN_VALUE))
//        val gamma = log10(next.magnitude.coerceAtLeast(Float.MIN_VALUE))
//
//        val denominator = alpha - 2 * beta + gamma
//        if (denominator == 0f) return current
//
//        val delta = 0.5f * (alpha - gamma) / denominator
//        val binWidth = next.frequency - current.frequency
//
//        return FrequencyBin(
//            frequency = current.frequency + delta * binWidth,
//            magnitude = 10f.pow(beta - 0.25f * (alpha - gamma) * delta),
//        )
//    }
//
//    private fun mergeComponents(
//        components: List<FrequencyBin>,
//        binSpacing: Float,
//    ): FrequencyBins {
//        if (components.isEmpty()) return emptyList()
//
//        val mergeTolerance = binSpacing * 0.5f
//
//        return components
//            .sortedBy { it.frequency }
//            .fold(mutableListOf<MutableList<FrequencyBin>>()) { groups, component ->
//                val lastGroup = groups.lastOrNull()
//                if (lastGroup != null &&
//                    (component.frequency - lastGroup.last().frequency) < mergeTolerance
//                ) {
//                    lastGroup.add(component)
//                } else {
//                    groups.add(mutableListOf(component))
//                }
//                groups
//            }
//            .map { group ->
//                val totalMagnitude = group.sumOf { it.magnitude.toDouble() }.toFloat()
//                val weightedFrequency = group
//                    .sumOf { (it.frequency * it.magnitude).toDouble() }
//                    .toFloat() / totalMagnitude
//                FrequencyBin(weightedFrequency, totalMagnitude)
//            }
//    }
//}
//
//class WindowPointSpreadFunction private constructor(
//    private val responseReal: FloatArray,
//    private val responseImaginary: FloatArray,
//    private val oversampleFactor: Int,
//) {
//
//    fun realAt(binOffset: Float): Float = interpolate(responseReal, binOffset)
//
//    fun imaginaryAt(binOffset: Float): Float {
//        val value = interpolate(responseImaginary, abs(binOffset))
//        return if (binOffset >= 0) value else -value
//    }
//
//    private fun interpolate(response: FloatArray, binOffset: Float): Float {
//        val index = abs(binOffset) * oversampleFactor
//        val halfLength = response.size / 2
//
//        val lowerIndex = index.toInt()
//        if (lowerIndex >= halfLength) return 0f
//
//        val upperIndex = lowerIndex + 1
//        if (upperIndex >= halfLength) return response[lowerIndex]
//
//        val fraction = index - lowerIndex
//        return response[lowerIndex] + fraction * (response[upperIndex] - response[lowerIndex])
//    }
//
//    companion object {
//        fun fromWindowCoefficients(
//            coefficients: FloatArray,
//            fftLength: Int,
//            oversampleFactor: Int = 16,
//        ): WindowPointSpreadFunction {
//            val paddedLength = max(coefficients.size, fftLength) * oversampleFactor
//            val spectrum = FftFrequencyBinsCalculator().calculate(
//                monoSamples = FloatArray(paddedLength).also { coefficients.copyInto(it) },
//                sampleRate = 1f,
//                magnitudeCorrectionFactor = 1f
//            )
//
//            val peakMagnitude = (0 until spectrum.size).maxOf { spectrum }
//
//            return WindowPointSpreadFunction(
//                responseReal = FloatArray(spectrum.size) { spectrum.real[it] / peakMagnitude },
//                responseImaginary = FloatArray(spectrum.size) { spectrum.imaginary[it] / peakMagnitude },
//                oversampleFactor = oversampleFactor,
//            )
//        }
//    }
//}
//
//object FftCalculator {
//
//    fun complexSpectrum(samples: FloatArray, sampleRate: Float): ComplexSpectrum {
//        val fftData = realForward(samples)
//        val binCount = fftData.size / 2
//        val scale = 2f / samples.size
//
//        return ComplexSpectrum(
//            real = FloatArray(binCount) { i -> fftData[i * 2] * scale },
//            imaginary = FloatArray(binCount) { i -> fftData[i * 2 + 1] * scale },
//            binSpacing = sampleRate / samples.size,
//        )
//    }
//
//    fun magnitudeSpectrum(samples: FloatArray): FloatArray {
//        val fftData = realForward(samples)
//        val binCount = fftData.size / 2
//
//        return FloatArray(binCount) { i ->
//            val real = fftData[i * 2]
//            val imaginary = fftData[i * 2 + 1]
//            sqrt(real * real + imaginary * imaginary) * 2 / samples.size
//        }
//    }
//
//    private fun realForward(samples: FloatArray): FloatArray {
//        val output = samples.copyOf()
//        FloatFFT_1D(output.size.toLong()).realForward(output)
//        return output
//    }
//}
//
//class ComplexSpectrum(
//    val real: FloatArray,
//    val imaginary: FloatArray,
//    val binSpacing: Float,
//) {
//    val size: Int get() = real.size
//
//    fun magnitudeAt(index: Int): Float {
//        val r = real[index]
//        val i = imaginary[index]
//        return sqrt(r * r + i * i)
//    }
//
//    fun frequencyAt(index: Int): Float = index * binSpacing
//
//    fun toFrequencyBins(magnitudeCorrectionFactor: Float): FrequencyBins {
//        return List(size) { index ->
//            FrequencyBin(
//                frequency = frequencyAt(index),
//                magnitude = magnitudeAt(index) * magnitudeCorrectionFactor,
//            )
//        }
//    }
//}