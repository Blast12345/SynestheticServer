package lightOrgan

import color.StandardRgbColor
import color.StandardRgbColors
import config.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import lightOrgan.color.ColorCalculator
import lightOrgan.gateway.Gateway
import lightOrgan.gateway.GatewayManager
import lightOrgan.input.AudioInputManager
import lightOrgan.spectralAnalysis.SpectralAnalysis
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import logging.Logger
import utilities.TimestampUtility
import utilities.coroutines.Sequenced
import utilities.coroutines.mapSequenced
import utilities.coroutines.onEachSequenced

// ENHANCEMENT: Gracefully handle crashed coroutines
class LightOrgan(
    private val inputManager: AudioInputManager,
    private val spectralAnalyzer: SpectralAnalyzer,
    private val colorCalculator: ColorCalculator,
    private val gatewayManager: GatewayManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
) {

    private val _spectralAnalysis = MutableStateFlow(SpectralAnalysis.EMPTY)
    val spectralAnalysis: StateFlow<SpectralAnalysis> = _spectralAnalysis.asStateFlow()

    private val _color = MutableStateFlow(StandardRgbColors.Black)
    val color: StateFlow<StandardRgbColor> = _color.asStateFlow()

    fun start(
        config: () -> AppConfig
    ) {
        val timeBetweenColors = TimestampUtility("Time between colors")

        inputManager.audioStream
            .buffer(64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .mapSequenced("Spectral analysis") { spectralAnalyzer.analyze(it, config().spectralAnalysis) }
            .onEach { _spectralAnalysis.value = it.value }
            .conflate()
            .mapSequenced("Color generation") { colorCalculator.calculate(it) }
            .onEach { _color.value = it.value }
            .conflate()
            .onEachSequenced("Gateway broadcast") { gatewayManager.gateway?.broadcastColor(it) }
            .onEach { timeBetweenColors.logTimeSinceLast() }
            .launchIn(scope)
    }

    // Convenience
    private val GatewayManager.gateway: Gateway?
        get() = (this.state.value as? GatewayManager.State.Connected)?.gateway

    private fun <T, R> Flow<Sequenced<T>>.mapSequenced(
        label: String,
        transform: suspend (T) -> R
    ): Flow<Sequenced<R>> = mapSequenced(
        transform = transform,
        onGap = { Logger.warning("$label is slow, dropped $it") }
    )

    private fun <T> Flow<Sequenced<T>>.onEachSequenced(
        label: String,
        action: suspend (T) -> Unit
    ): Flow<Sequenced<T>> = onEachSequenced(
        action = action,
        onGap = { Logger.warning("$label is slow, dropped $it") }
    )

}