import androidx.compose.material.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import color.StandardRgbColor
import config.AppConfigSingleton
import controls.ServerCommandListener
import gui.Theme
import gui.dashboard.Dashboard
import gui.dashboard.DashboardViewModel
import gui.dashboard.tiles.color.ColorTileViewModel
import gui.dashboard.tiles.spectralAnalysis.SpectralAnalysisTileViewModel
import gui.snackbar.SimpleSnackbar
import gui.tiles.audioInput.AudioInputTileViewModel
import gui.tiles.gateway.GatewayTileViewModel
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import lightOrgan.LightOrgan
import lightOrgan.color.ColorCalculator
import lightOrgan.gateway.GatewayManager
import lightOrgan.gateway.RealGatewayManager
import lightOrgan.input.AudioInputManager
import lightOrgan.input.RealAudioInputManager
import lightOrgan.spectralAnalysis.SpectralAnalysis
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import logging.LogLevel
import logging.Logger
import kotlin.time.Duration.Companion.seconds

// ENHANCEMENT: Persist config between app launches
// ENHANCEMENT: Make state machines (e.g. managers) thread safe. Maybe create a state wrapper that uses a mutex.
// ENHANCEMENT: Explore higher baud rates
// ENHANCEMENT: Introduce Frequency type
// ENHANCEMENT: Introduce SampleRate type (which exposes nyquistFrequency)
// ENHANCEMENT: Introduce Magnitude and DBFS types (which can be converted back and forth)
// ENHANCEMENT: Bump JVM SDK version to 21
fun main(args: Array<String>) {
    configureLogger(args)

    // Initialize
    val inputManager = RealAudioInputManager()
    val spectralAnalyzer = SpectralAnalyzer()
    val colorCalculator = ColorCalculator()
    val gatewayManager = RealGatewayManager()
    val lightOrgan = LightOrgan(inputManager, spectralAnalyzer, colorCalculator, gatewayManager)
    val serverCommandListener = ServerCommandListener()

    // Teardown
    registerShutdownActions(
        { serverCommandListener.stop() },
        { inputManager.stopListening() },
        { gatewayManager.disconnect() }, // TODO: Make more reliable? Wait for disconnect or no-op if already disconnected.
    )

    // Start
    lightOrgan.start(config = { AppConfigSingleton.value })
    serverCommandListener.start() // TODO: If no source can be found, then log error?

    // Launch
    if (args.contains("--headless")) {
        launchHeadless(inputManager, gatewayManager)
    } else {
        launchGUI(inputManager, lightOrgan.spectralAnalysis, lightOrgan.color, gatewayManager)
    }
}

private fun configureLogger(args: Array<String>) {
    Logger.level = when {
        args.contains("--verbose") -> LogLevel.DEBUG
        args.contains("--quiet") -> LogLevel.ERROR
        else -> LogLevel.SUCCESS
    }
}

private fun launchGUI(
    inputManager: AudioInputManager,
    spectralAnalysis: StateFlow<SpectralAnalysis>,
    color: StateFlow<StandardRgbColor>,
    gatewayManager: GatewayManager,
) = application {
    val minimumWidth = 1200
    val minimumHeight = 300

    Window(
        title = "Synesthetic",
        state = rememberWindowState(
            width = minimumWidth.dp,
            height = minimumHeight.dp,
        ),
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = java.awt.Dimension(minimumWidth, minimumHeight)

        Theme {
            val snackbar = remember { SimpleSnackbar() }

            Scaffold(
                snackbarHost = { snackbar.Host() }
            ) {
                val viewModel = remember {
                    DashboardViewModel(
                        AudioInputTileViewModel(inputManager, snackbar.controller),
                        SpectralAnalysisTileViewModel(spectralAnalysis),
                        ColorTileViewModel(color),
                        GatewayTileViewModel(gatewayManager, snackbar.controller),
                    )
                }

                Dashboard(viewModel)
            }
        }
    }
}

private fun launchHeadless(
    inputManager: AudioInputManager,
    gatewayManager: GatewayManager,
) {
    runBlocking {
        inputManager.startListening()
        gatewayManager.connect()
        awaitCancellation()
    }
}

private fun registerShutdownActions(vararg teardowns: suspend () -> Unit) {
    // TODO: Make these fault tolerant; one failure should cause others to fail.
    Runtime.getRuntime().addShutdownHook(
        Thread {
            runBlocking {
                teardowns.forEach { teardown ->
                    runCatching { withTimeout(1.seconds) { teardown() } }.onFailure { Logger.error(it) }
                }
            }
        }
    )
}