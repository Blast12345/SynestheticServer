package lightOrgan.input

import audio.audioInput.AudioInput
import audio.audioInput.AudioInputFinder
import audio.samples.SequencedAudioFrame
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*

interface AudioInputManager {
    val inputDetails: StateFlow<AudioInputDetails?>
    val isListening: StateFlow<Boolean>
    val audioStream: SharedFlow<SequencedAudioFrame>
    fun startListening()
    fun stopListening()
}

// ENHANCEMENT: Handle unexpected disconnects.
@OptIn(ExperimentalCoroutinesApi::class)
class RealAudioInputManager(
    private val currentAudioInput: MutableStateFlow<AudioInput?> = MutableStateFlow(null),
    private val audioInputFinder: AudioInputFinder = AudioInputFinder(),
    private val scope: CoroutineScope = CoroutineScope(SupervisorJob())
) : AudioInputManager {

    override val inputDetails: StateFlow<AudioInputDetails?> = currentAudioInput
        .map { it?.let { AudioInputDetails(it.name, it.format) } }
        .stateIn(scope, SharingStarted.Eagerly, null)

    override val isListening: StateFlow<Boolean> = currentAudioInput
        .flatMapLatest { it?.isListening ?: flowOf(false) }
        .stateIn(scope, SharingStarted.Eagerly, false)

    override val audioStream: SharedFlow<SequencedAudioFrame> = currentAudioInput
        .flatMapLatest { it?.audioStream ?: emptyFlow() }
        .shareIn(scope, SharingStarted.Eagerly)

    // Start-stop
    override fun startListening() {
        if (currentAudioInput.value == null) {
            currentAudioInput.value = audioInputFinder.findDefaultInput()
        }

        val input = currentAudioInput.value ?: throw IllegalStateException("Cannot start listening. No input selected.")
        input.start()
    }

    override fun stopListening() {
        val input = currentAudioInput.value ?: throw IllegalStateException("Cannot stop listening. No input selected.")
        input.stop()
    }

}