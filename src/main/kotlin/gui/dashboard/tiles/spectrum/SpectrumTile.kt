package gui.dashboard.tiles.spectrum

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.text.font.FontWeight
import dsp.bins.FrequencyBin
import dsp.bins.FrequencyBins
import dsp.peakExtraction.SpectralPeaks
import gui.basicComponents.*
import org.jetbrains.skia.FilterBlurMode
import org.jetbrains.skia.MaskFilter
import kotlin.math.round

@Preview
@Composable
fun SpectrumTile(
    viewModel: SpectrumTileViewModel,
    modifier: Modifier = Modifier
) {
    Tile(modifier) {
        Title()
        SimpleSpacer(dpSize = 12)
        HighlightedFrequency(bin = viewModel.highlightedBin) // TODO: Make follow cursor
        SimpleSpacer(dpSize = 12)

        Grid {
            Spectrum(viewModel)
        }
    }
}

@Composable
private fun Title() {
    SimpleText(
        text = "Spectrum",
        fontSize = 24,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun HighlightedFrequency(bin: FrequencyBin?) {
    DetailText(
        label = "Frequency",
        value = bin?.frequency?.let { "%.2f Hz".format(it) }
    )
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Spectrum(viewModel: SpectrumTileViewModel) {
    val bins = viewModel.displayedBins.collectAsState()
    val peaks = viewModel.displayedPeaks.collectAsState()
    val binCount = remember { derivedStateOf { bins.value.size } } // optimization
    val hoveredIndex = viewModel.highlightedIndex
    val spectrumColor = MaterialTheme.colors.secondary
    val peakColor = Color.White

    val lowestFrequency = bins.value.minOfOrNull { it.frequency } ?: 0f
    val highestFrequency = bins.value.maxOfOrNull { it.frequency } ?: 0f

    if (bins.value.count() == 0) {
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds()
            .onBinHover(
                binCount = binCount.value,
                onHover = { viewModel.highlightedIndex = it },
                onExit = { viewModel.highlightedIndex = null }
            )
    ) {
        val barWidth = size.width / bins.value.size

        drawSpectrum(bins.value, spectrumColor, barWidth)
        drawPeaks(peaks.value, lowestFrequency, highestFrequency, peakColor)
        drawHoverHighlight(hoveredIndex, barWidth)
    }
}

// ENHANCEMENT: Add option for parabolic interpolation
private fun DrawScope.drawSpectrum(bins: FrequencyBins, color: Color, barWidth: Float) {
    bins.forEachIndexed { index, bin ->
        val xLeft = index * barWidth
        val xRight = (index + 1) * barWidth
        val height = bin.magnitude * size.height

        val snappedLeft = round(xLeft)
        val snappedRight = round(xRight)
        val snappedHeight = round(height)
        val snappedWidth = snappedRight - snappedLeft

        drawRect(
            color = color,
            topLeft = Cartesian(snappedLeft, snappedHeight).to(this),
            size = Size(snappedWidth, snappedHeight)
        )
    }
}

private fun DrawScope.drawPeaks(peaks: SpectralPeaks, lowestFrequency: Float, highestFrequency: Float, color: Color) {
    val peakStrokeWidth = 1f
    val glowStrokeWidth = peakStrokeWidth + 2f
    val halfStroke = peakStrokeWidth / 2f

    peaks.forEach { peak ->
        val positionInRange = (peak.frequency - lowestFrequency) / (highestFrequency - lowestFrequency)

        val centerX = positionInRange * size.width
        val lineHeight = peak.magnitude * size.height

        val snappedCenterX = round(centerX - halfStroke) + halfStroke
        val snappedLineHeight = round(lineHeight)

        // Glow
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                this.color = color
                strokeWidth = glowStrokeWidth
                style = PaintingStyle.Stroke
                asFrameworkPaint().maskFilter = MaskFilter.makeBlur(FilterBlurMode.NORMAL, 4f)
            }

            canvas.drawLine(
                Cartesian(snappedCenterX, 0f).to(this),
                Cartesian(snappedCenterX, snappedLineHeight).to(this),
                paint
            )
        }

        // Defined line
        drawLine(
            color,
            Cartesian(snappedCenterX, 0f).to(this),
            Cartesian(snappedCenterX, snappedLineHeight).to(this),
            peakStrokeWidth
        )
    }
}

private fun DrawScope.drawHoverHighlight(hoveredIndex: Int?, barWidth: Float) {
    hoveredIndex?.let { index ->
        val xLeft = index * barWidth
        val xRight = (index + 1) * barWidth

        val snappedLeft = round(xLeft)
        val snappedRight = round(xRight)
        val snappedWidth = snappedRight - snappedLeft

        drawRect(
            color = Color.White.copy(alpha = 0.5f),
            topLeft = Cartesian(snappedLeft, size.height).to(this),
            size = Size(snappedWidth, size.height)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
private fun Modifier.onBinHover(
    binCount: Int,
    onHover: (Int) -> Unit,
    onExit: () -> Unit
): Modifier {
    return this
        .onPointerEvent(PointerEventType.Move) {
            val index = (it.changes.first().position.x / size.width * binCount).toInt()
            onHover(index)
        }
        .onPointerEvent(PointerEventType.Exit) { onExit() }
}


// TODO: Move me
data class Cartesian(val x: Float, val y: Float)

fun Cartesian.to(scope: DrawScope) = Offset(x, scope.size.height - y)