package gui.dashboard.tiles.spectralAnalysis

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dsp.bins.FrequencyBin
import lightOrgan.spectralAnalysis.SpectralAnalyzer

// ENHANCEMENT: Show latency
class SpectralAnalysisTileViewModel(
    private val spectralAnalyzer: SpectralAnalyzer
) {

    val analysis = spectralAnalyzer.analysis

    var highlightedIndex: Int? by mutableStateOf(null)
    val highlightedBin: FrequencyBin? get() = highlightedIndex?.let { analysis.value.spectrum.getOrNull(it) }

}