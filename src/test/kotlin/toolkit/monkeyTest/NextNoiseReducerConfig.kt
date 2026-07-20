package toolkit.monkeyTest

import lightOrgan.spectralAnalysis.noiseReduction.NoiseReducer
import lightOrgan.spectralAnalysis.noiseReduction.SpectralGate
import kotlin.random.Random

fun nextNoiseReducerConfig(): NoiseReducer.Config {
    return listOf(
        nextSpectralGateConfig()
    ).random()
}

fun nextSpectralGateConfig(): SpectralGate.Config {
    return SpectralGate.Config(
        thresholdDb = Random.nextDouble()
    )
}