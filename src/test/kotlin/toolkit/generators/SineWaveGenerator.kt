package toolkit.generators

import extensions.inSeconds
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class SineWave(
    val frequency: Float,
    val amplitude: Float,
    val waveForm: WaveForm
)

fun generateSineWave(
    frequency: Float,
    amplitude: Float = 1f,
    sampleRate: Float,
    duration: Duration = 1.seconds,
): SineWave {
    val sampleSize = (sampleRate * duration.inSeconds).toInt()

    return SineWave(
        frequency = frequency,
        amplitude = amplitude,
        waveForm = WaveForm(
            sampleRate = sampleRate,
            samples = FloatArray(sampleSize) { i -> amplitude * sin(2.0 * PI * frequency * i / sampleRate).toFloat() }
        )
    )
}

fun generateSineWave(
    frequency: Float,
    amplitudeDb: Double,
    sampleRate: Float,
    duration: Duration = 1.seconds,
): SineWave = generateSineWave(
    frequency = frequency,
    amplitude = amplitudeDb.dbToLinear().toFloat(),
    sampleRate = sampleRate,
    duration = duration,
)

fun Double.dbToLinear(): Double = 10.0.pow(this / 20.0)