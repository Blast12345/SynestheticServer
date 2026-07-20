package gui.dashboard.tiles.color

import color.StandardRgbColor
import kotlinx.coroutines.flow.StateFlow

// ENHANCEMENT: Consider showing Note ring, Color wheel, and color in the center.
// ENHANCEMENT: Consider showing the helical pitch model https://www.researchgate.net/figure/The-helical-model-of-pitch-Musical-pitch-is-depicted-as-varying-along-both-a-linear_fig1_272318954
class ColorTileViewModel(
    val color: StateFlow<StandardRgbColor>
)