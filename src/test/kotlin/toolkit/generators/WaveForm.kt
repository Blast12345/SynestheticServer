package toolkit.generators

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class WaveForm(
    val sampleRate: Float,
    val samples: FloatArray
) {
    fun toAudioFrame(): AudioFrame = AudioFrame(samples, AudioFormat(sampleRate, 16, 1))
}

fun generateSilence(sampleRate: Float, duration: Duration = 1.seconds): WaveForm {
    val sampleSize = (sampleRate * duration.inWholeSeconds).toInt()

    return WaveForm(
        sampleRate = sampleRate,
        samples = FloatArray(sampleSize)
    )
}

fun combineWaves(vararg waves: WaveForm): WaveForm {
    require(waves.isNotEmpty())
    require(waves.all { it.sampleRate == waves[0].sampleRate })
    require(waves.all { it.samples.size == waves[0].samples.size })

    return WaveForm(
        sampleRate = waves[0].sampleRate,
        samples = FloatArray(waves[0].samples.size) { i ->
            waves.sumOf { it.samples[i].toDouble() }.toFloat()
        }
    )
}