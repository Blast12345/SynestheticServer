package toolkit.monkeyTest

import lightOrgan.spectralAnalysis.conditioning.DecimationConfig

fun nextDecimationConfig(): DecimationConfig {
    return listOf(
        nextAutomaticDecimationConfig(),
        nextExplicitDecimationConfig()
    ).random()
}

fun nextAutomaticDecimationConfig(): DecimationConfig.Automatic {
    return DecimationConfig.Automatic(nextPositiveFloat())
}

fun nextExplicitDecimationConfig(): DecimationConfig.Explicit {
    return DecimationConfig.Explicit(nextPositiveFloat())
}