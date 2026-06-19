package dsp

import audio.samples.AudioFrame

class Decimator {

    private var phaseOffset: Int = 0
    private var lastFactor: Int = 0
    private var lastChannels: Int = 0
    private var lastSampleRate: Float = 0f

    fun decimationFactor(sampleRate: Float, targetNyquist: Float): Int {
        val factor = (sampleRate / (2 * targetNyquist)).toInt()
        require(factor >= 1) { "Target Nyquist frequency ($targetNyquist) exceeds sample rate capability ($sampleRate)" }
        return factor
    }

    fun decimate(audio: AudioFrame, factor: Int): AudioFrame {
        val decimatedSamples = decimate(
            samples = audio.samples,
            sampleRate = audio.format.sampleRate,
            channels = audio.format.channels,
            factor = factor
        )

        val effectiveSampleRate = audio.format.sampleRate / factor
        
        return AudioFrame(
            decimatedSamples,
            audio.format.copy(sampleRate = effectiveSampleRate)
        )
    }

    private fun decimate(samples: FloatArray, sampleRate: Float, channels: Int, factor: Int): FloatArray {
        require(factor >= 1) { "Decimation factor must be positive, got $factor" }
        require(channels >= 1) { "Channel count must be positive, got $channels" }
        resetIfNeeded(factor, sampleRate, channels)

        if (factor == 1) return samples

        // A "frame" in this context is a group of samples that represent a single moment in time.
        // E.g., 2 channels means it takes 2 samples to represent a single moment in time.
        val totalFrames = samples.size / channels

        // Decimation works by skipping frames, relative to the factor
        val framesToPick = phaseOffset until totalFrames step factor

        if (framesToPick.isEmpty()) {
            phaseOffset -= totalFrames
            return FloatArray(0)
        } else {
            val output = FloatArray(framesToPick.count() * channels)

            framesToPick.forEachIndexed { outputFrame, sourceFrame ->
                for (channel in 0 until channels) {
                    output[outputFrame * channels + channel] = samples[sourceFrame * channels + channel]
                }
            }

            phaseOffset = framesToPick.last + factor - totalFrames
            return output
        }
    }

    private fun resetIfNeeded(factor: Int, sampleRate: Float, channels: Int) {
        if (factor != lastFactor || channels != lastChannels || sampleRate != lastSampleRate) {
            phaseOffset = 0
            lastFactor = factor
            lastChannels = channels
            lastSampleRate = sampleRate
        }
    }

}