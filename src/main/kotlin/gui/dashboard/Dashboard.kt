package gui.dashboard

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import gui.dashboard.tiles.color.ColorTile
import gui.dashboard.tiles.spectralAnalysis.SpectralAnalysisTile
import gui.tiles.audioInput.AudioInputTile
import gui.tiles.gateway.GatewayTile

@Preview
@Composable
fun Dashboard(
    viewModel: DashboardViewModel
) {
    Background()

    MainRow(
        viewModel,
        Modifier.fillMaxSize().padding(16.dp)
    )
}

@Composable
private fun Background() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    )
}

@Composable
private fun MainRow(
    viewModel: DashboardViewModel,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {

        AudioInputTile(
            viewModel = viewModel.audioInputTileViewModel,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )

        SpectralAnalysisTile(
            viewModel = viewModel.spectralAnalysisTileViewModel,
            modifier = Modifier.weight(1f).fillMaxSize()
        )

        ColorTile(
            viewModel = viewModel.colorTileViewModel,
            modifier = Modifier.weight(1f).fillMaxSize()
        )

        GatewayTile(
            viewModel = viewModel.gatewayTileViewModel,
            modifier = Modifier.weight(1f).fillMaxHeight()
        )

    }
}
