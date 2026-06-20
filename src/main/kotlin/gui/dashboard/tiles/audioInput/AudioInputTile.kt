package gui.tiles.audioInput

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import gui.basicComponents.*

@Preview
@Composable
fun AudioInputTile(
    viewModel: AudioInputTileViewModel,
    modifier: Modifier = Modifier
) {
    val inputDetails by viewModel.inputDetails.collectAsState()
    val isListening by viewModel.isListening.collectAsState()

    Tile(modifier) {
        SimpleText(
            text = "Audio Input",
            fontSize = 24,
            fontWeight = FontWeight.SemiBold
        )

        SimpleSpacer(dpSize = 12)

        DetailText("Status", if (isListening) "Listening" else "Not listening")
        DetailText("Name", inputDetails?.name ?: "")
        DetailText("Sample Rate", inputDetails?.format?.sampleRate?.toString() ?: "")
        DetailText("Bit Depth", inputDetails?.format?.bitDepth?.toString() ?: "")

        SimpleSpacer(dpSize = 12)

        if (isListening) {
            SimpleButton(
                title = "Stop",
                isLoading = false,
                action = { viewModel.stop() }
            )
        } else {
            SimpleButton(
                title = "Start",
                isLoading = false,
                action = { viewModel.start() }
            )
        }
    }
}