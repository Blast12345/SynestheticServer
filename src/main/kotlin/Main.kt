import androidx.compose.material.Scaffold
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.github.kwhat.jnativehook.GlobalScreen
import gui.Theme
import gui.dashboard.Dashboard
import gui.dashboard.DashboardViewModel
import gui.snackbar.SimpleSnackbar
import hotkeys.GainHotkeyListener
import hotkeys.NoiseReductionHotkeyListener
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.runBlocking
import lightOrgan.LightOrgan
import lightOrgan.color.ColorManager
import lightOrgan.gateway.GatewayManager
import lightOrgan.gateway.RealGatewayManager
import lightOrgan.input.AudioInputManager
import lightOrgan.spectralAnalysis.SpectralAnalyzer
import logging.LogLevel
import logging.Logger

// ENHANCEMENT: Persist config between app launches
// ENHANCEMENT: Make state machines (e.g. managers) thread safe. Maybe create a state wrapper that uses a mutex.
// ENHANCEMENT: Explore higher baud rates
// ENHANCEMENT: Introduce Frequency type
// ENHANCEMENT: Introduce SampleRate type (which exposes nyquistFrequency)
// ENHANCEMENT: Introduce Magnitude and DBFS types (which can be converted back and forth)
// ENHANCEMENT: Bump JVM SDK version to 21
fun main(args: Array<String>) {
    configureLogger(args)
    addHotkeyListeners()

    val inputManager = AudioInputManager()
    val spectralAnalyzer = SpectralAnalyzer()
    val colorManager = ColorManager()
    val gatewayManager = RealGatewayManager()

    val lightOrgan = LightOrgan(inputManager, spectralAnalyzer, colorManager, gatewayManager)
    lightOrgan.start()

    if (args.contains("--headless")) {
        launchHeadless(inputManager, gatewayManager)
    } else {
        launchGUI(inputManager, spectralAnalyzer, colorManager, gatewayManager)
    }
}

private fun configureLogger(args: Array<String>) {
    Logger.level = when {
        args.contains("--verbose") -> LogLevel.DEBUG
        args.contains("--quiet") -> LogLevel.ERROR
        else -> LogLevel.SUCCESS
    }
}

private fun addHotkeyListeners() {
    GlobalScreen.registerNativeHook()

    GlobalScreen.addNativeKeyListener(GainHotkeyListener())
    GlobalScreen.addNativeKeyListener(NoiseReductionHotkeyListener())
}

private fun launchGUI(
    inputManager: AudioInputManager,
    spectralAnalyzer: SpectralAnalyzer,
    colorManager: ColorManager,
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
                        inputManager,
                        spectralAnalyzer,
                        colorManager,
                        gatewayManager,
                        snackbar.controller
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
    Runtime.getRuntime().addShutdownHook(Thread {
        runBlocking { gatewayManager.disconnect() }
        inputManager.stopListening()
    })

    runBlocking {
        inputManager.startListening()
        gatewayManager.connect()
        awaitCancellation()
    }
}