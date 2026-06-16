package gui.dashboard.tiles.spectrum

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dsp.bins.FrequencyBin
import lightOrgan.spectrum.SpectrumManager

// ENHANCEMENT: Show latency
class SpectrumTileViewModel(
    private val spectrumManager: SpectrumManager
) {

    val spectralAnalysis = spectrumManager.spectralAnalysis

    var highlightedIndex: Int? by mutableStateOf(null)
    val highlightedBin: FrequencyBin? get() = highlightedIndex?.let { spectralAnalysis.value.spectrum.getOrNull(it) }

}