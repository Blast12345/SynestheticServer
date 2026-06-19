package toolkit.monkeyTest

import audio.samples.AudioFormat
import audio.samples.AudioFrame

fun nextAudioFrame(
    samples: FloatArray = nextFloatArray(),
    format: AudioFormat = nextAudioFormat()
): AudioFrame {
    return AudioFrame(
        samples = samples,
        format = format
    )
}

fun nextAudioFrame(
    channels: List<FloatArray> = listOf(nextFloatArray()),
    sampleRate: Float = nextPositiveFloat(),
    bitDepth: Int = nextInt()
): AudioFrame {
    require(channels.isNotEmpty())
    val frameSize = channels[0].size
    require(channels.all { it.size == frameSize })

    val interleaved = FloatArray(frameSize * channels.size) { i ->
        channels[i % channels.size][i / channels.size]
    }

    return AudioFrame(
        samples = interleaved,
        format = AudioFormat(
            sampleRate = sampleRate,
            bitDepth = bitDepth,
            channels = channels.size
        )
    )
}
