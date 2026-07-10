package gui.dashboard.tiles.spectralAnalysis

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dsp.bins.FrequencyBin
import kotlinx.coroutines.flow.StateFlow
import lightOrgan.spectralAnalysis.SpectralAnalysis

// ENHANCEMENT: Show latency
// ENHANCEMENT: Show the filter response in the UI
class SpectralAnalysisTileViewModel(
    val analysis: StateFlow<SpectralAnalysis>
) {

    var highlightedIndex: Int? by mutableStateOf(null)
    val highlightedBin: FrequencyBin? get() = highlightedIndex?.let { analysis.value.spectrum.getOrNull(it) }

}