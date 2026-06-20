package audio.audioInput

import audio.audioDevice.AudioDevice
import audio.audioDevice.AudioDeviceFinder
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class AudioInputFinderTests {

    private val audioDevice1: AudioDevice = mockk()
    private val audioDevice2: AudioDevice = mockk()
    private val allAudioDevices: List<AudioDevice> = listOf(audioDevice1, audioDevice2)

    private val input1: AudioInput = mockk()
    private val input2: AudioInput = mockk()
    private val input3: AudioInput = mockk()
    private val allInputs = setOf(input1, input2, input3)

    private val audioDeviceFinder: AudioDeviceFinder = mockk()

    @BeforeEach
    fun setupHappyPath() {
        every { audioDevice1.inputs } returns listOf(input1, input2)
        every { audioDevice2.inputs } returns listOf(input3)

        every { audioDeviceFinder.findAll() } returns allAudioDevices
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(): AudioInputFinder {
        return AudioInputFinder(
            audioDeviceFinder = audioDeviceFinder
        )
    }

    // All inputs
    @Test
    fun `given there are inputs, when finding all inputs, then return all inputs`() {
        val sut = createSUT()

        val actual = sut.findAll()

        assertEquals(allInputs, actual.toSet())
    }

    @Test
    fun `given there are no inputs, when finding all inputs, then return an empty list`() {
        val sut = createSUT()
        every { audioDevice1.inputs } returns emptyList()
        every { audioDevice2.inputs } returns emptyList()

        val actual = sut.findAll()

        assertEquals(true, actual.isEmpty())
    }

    // Default Input
    @Test
    fun `given there are inputs, when finding default input, then return the first input`() {
        val sut = createSUT()

        val actual = sut.findDefaultInput()

        assertEquals(input1, actual)
    }

    @Test
    fun `given there are no inputs, when finding default input, then return null`() {
        val sut = createSUT()
        every { audioDevice1.inputs } returns emptyList()
        every { audioDevice2.inputs } returns emptyList()

        val actual = sut.findDefaultInput()

        assertNull(actual)
    }

}