package toolkit.generators

import audio.samples.AudioFormat
import audio.samples.AudioFrame

class Tone(
    val frequency: Float,
    val amplitude: Float = 1f
)

class TestToneGenerator(
    val format: AudioFormat
) {

    companion object {
        fun mono(sampleRate: Float = 48000f) =
            TestToneGenerator(AudioFormat(sampleRate, bitDepth = 16, channels = 1))

        fun stereo(sampleRate: Float = 48000f) =
            TestToneGenerator(AudioFormat(sampleRate, bitDepth = 16, channels = 2))
    }

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