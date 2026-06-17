package lightOrgan.color

import color.rgb.StandardRgbColor
import dsp.peakExtraction.SpectralPeaks

interface ColorAlgorithm {
    fun calculate(spectralPeaks: SpectralPeaks): StandardRgbColor
}