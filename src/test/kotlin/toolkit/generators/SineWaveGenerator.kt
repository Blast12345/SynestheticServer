package toolkit.generators

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import extensions.inSeconds
import kotlin.math.PI
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

// TODO: Make the tone generator live in the real project?
class Tone(
    val frequency: Float,
    val amplitude: Float = 1f
)

class TestToneGenerator(
    val format: AudioFormat = AudioFormat(sampleRate = 48000f, bitDepth = 16, channels = 1),
) {

    fun silence(): AudioFrame {
        return AudioFrame(generateSilence(format.sampleRate).samples, format)
    }

    fun generate(vararg tones: Tone): AudioFrame {
        require(tones.isNotEmpty()) { "At least one tone is required." }

        val combined = tones
            .map { generateSineWave(it.frequency, amplitude = it.amplitude, sampleRate = format.sampleRate).waveForm }
            .reduce { accumulated, wave -> combineWaves(accumulated, wave) }

        return AudioFrame(combined.samples, format)
    }

}