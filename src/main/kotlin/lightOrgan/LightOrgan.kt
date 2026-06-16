package lightOrgan

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.*
import lightOrgan.color.ColorManager
import lightOrgan.gateway.Gateway
import lightOrgan.gateway.GatewayManager
import lightOrgan.input.AudioInputManager
import lightOrgan.spectrum.SpectrumManager
import logging.Logger
import utilities.TimestampUtility
import utilities.coroutines.Sequenced
import utilities.coroutines.mapSequenced
import utilities.coroutines.onEachSequenced

// ENHANCEMENT: Gracefully handle crashed coroutines
class LightOrgan(
    private val inputManager: AudioInputManager,
    private val spectrumManager: SpectrumManager,
    private val colorManager: ColorManager,
    private val gatewayManager: GatewayManager,
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
) {

    private val timeBetweenColors = TimestampUtility("Time between colors")

    fun start() {
        inputManager.selectDefaultInput()

        inputManager.audioStream
            .buffer(64, onBufferOverflow = BufferOverflow.DROP_OLDEST)
            .mapSequenced("Spectral analysis") { spectrumManager.calculate(it) }
            .conflate()
            .mapSequenced("Color generation") { colorManager.calculate(it) }
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

    fun <T> Flow<Sequenced<T>>.onEachSequenced(
        label: String,
        action: suspend (T) -> Unit
    ): Flow<Sequenced<T>> = onEachSequenced(
        action = action,
        onGap = { Logger.warning("$label is slow, dropped $it") }
    )

}


