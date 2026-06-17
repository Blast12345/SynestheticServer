package lightOrgan.color

import color.StandardRgbColor
import dsp.peakExtraction.SpectralPeaks

interface ColorAlgorithm {
    fun calculate(spectralPeaks: SpectralPeaks): StandardRgbColor
}