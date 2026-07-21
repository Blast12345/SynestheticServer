package toolkit.generators

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class Tone(
    val frequency: Float,
    val amplitude: Float = 1f
)

class TestToneGenerator(
    val format: AudioFormat,
    val defaultDuration: Duration
) {

    companion object {
        fun mono(sampleRate: Float = 48000f, defaultDuration: Duration = 1.seconds) =
            TestToneGenerator(
                AudioFormat(sampleRate, bitDepth = 16, channels = 1),
                defaultDuration
            )

        fun stereo(sampleRate: Float = 48000f, defaultDuration: Duration = 1.seconds) =
            TestToneGenerator(
                AudioFormat(sampleRate, bitDepth = 16, channels = 2),
                defaultDuration
            )
    }

    fun silence(duration: Duration = defaultDuration): AudioFrame {
        val samples = generateSilence(format.sampleRate, duration).samples
        return AudioFrame(samples, format)
    }

    fun generate(vararg tones: Tone, duration: Duration = defaultDuration): AudioFrame {
        require(tones.isNotEmpty()) { "At least one tone is required." }

        val combined = tones
            .map { generateSineWave(it.frequency, amplitude = it.amplitude, sampleRate = format.sampleRate, duration).waveForm }
            .reduce { accumulated, wave -> combineWaves(accumulated, wave) }

        return AudioFrame(combined.samples, format)
    }

}