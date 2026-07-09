package lightOrgan.input

import audio.samples.AudioFormat
import audio.samples.AudioFrame
import audio.samples.SequencedAudioFrame
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import toolkit.generators.generateSineWave
import utilities.coroutines.asSequenced

class FakeAudioInputManager(
    initialInput: AudioInputDetails? = AudioInputDetails(
        name = "Fake input",
        format = AudioFormat(sampleRate = 44100f, bitDepth = 32, channels = 1),
    ),
) : AudioInputManager {

    override val inputDetails = MutableStateFlow(initialInput)
    override val isListening = MutableStateFlow(false)
    override val audioStream = MutableSharedFlow<SequencedAudioFrame>()

    // Start-stop
    var startError: Throwable? = null
    var stopError: Throwable? = null

    override fun startListening() {
        startError?.let { throw it }
        isListening.value = true
    }

    override fun stopListening() {
        stopError?.let { throw it }
        isListening.value = false
    }

    // Audio stream helpers
    var nextSequenceNumber = 0L

    private val currentFormat: AudioFormat
        get() = checkNotNull(inputDetails.value?.format) { "Cannot emit. No input selected." }

    suspend fun emit(samples: FloatArray) {
        val frame = AudioFrame(samples, currentFormat)
        audioStream.emit(frame.asSequenced(nextSequenceNumber++))
    }

    suspend fun emitTone(frequency: Float) {
        val waveForm = generateSineWave(frequency = frequency, sampleRate = currentFormat.sampleRate).waveForm
        emit(waveForm.samples)
    }

}