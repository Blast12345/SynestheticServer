package lightOrgan.spectralAnalysis

import dsp.filtering.Passband
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import lightOrgan.spectralAnalysis.conditioning.AudioConditioner
import lightOrgan.spectralAnalysis.noiseReduction.NoiseGate
import lightOrgan.spectralAnalysis.peaks.PeakExtractor
import lightOrgan.spectralAnalysis.spectrum.SpectrumCalculator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import toolkit.monkeyTest.nextAudioFrame
import toolkit.monkeyTest.nextFrequencyBin
import toolkit.monkeyTest.nextSpectralPeak

class SpectralAnalyzerUnitTests {

    private val audioConditioner: AudioConditioner = mockk()
    private val spectrumCalculator: SpectrumCalculator = mockk()
    private val peakExtractor: PeakExtractor = mockk()
    private val noiseGate: NoiseGate = mockk()

    private val audioFrame = nextAudioFrame()
    private val conditionedAudio = nextAudioFrame()

    private val bin1 = nextFrequencyBin(1f)
    private val bin2 = nextFrequencyBin(2f)
    private val bin3 = nextFrequencyBin(3f)
    private val bin4 = nextFrequencyBin(4f)
    private val allBins = listOf(bin1, bin2, bin3, bin4)

    private val peak1 = nextSpectralPeak(1f)
    private val peak2 = nextSpectralPeak(2f)
    private val peak3 = nextSpectralPeak(3f)
    private val peak4 = nextSpectralPeak(4f)
    private val allPeaks = listOf(peak1, peak2, peak3, peak4)

    @BeforeEach
    fun setupHappyPath() {
        every { audioConditioner.condition(audioFrame) } returns conditionedAudio
        every { spectrumCalculator.calculate(conditionedAudio) } returns allBins
        every { peakExtractor.extract(allBins) } returns allPeaks

        // Disable post-processing
        every { audioConditioner.passband } returns Passband.ALL
        every { noiseGate.apply(allBins) } returns allBins
        every { noiseGate.apply(allPeaks) } returns allPeaks
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    private fun createSUT(): SpectralAnalyzer {
        return SpectralAnalyzer(
            audioConditioner,
            spectrumCalculator,
            peakExtractor,
            noiseGate
        )
    }

    // Passband
    @Test
    fun `the spectrum is within the pass band`() {
        val sut = createSUT()
        every { audioConditioner.passband } returns Passband(lowerFrequency = bin2.frequency, higherFrequency = bin3.frequency)

        val result = sut.analyze(audioFrame)

        assertEquals(
            listOf(bin2, bin3),
            result.spectrum
        )
    }

    @Test
    fun `the peaks are within the pass band`() {
        val sut = createSUT()
        every { audioConditioner.passband } returns Passband(lowerFrequency = peak2.frequency, higherFrequency = peak3.frequency)

        val result = sut.analyze(audioFrame)

        assertEquals(
            listOf(peak2, peak3),
            result.peaks
        )
    }

    // Noise reduction
    @Test
    fun `the spectrum is denoised`() {
        val sut = createSUT()
        val denoisedBins = listOf(nextFrequencyBin(), nextFrequencyBin())
        every { noiseGate.apply(allBins) } returns denoisedBins

        val result = sut.analyze(audioFrame)

        assertEquals(denoisedBins, result.spectrum)
    }

    @Test
    fun `the peaks are denoised`() {
        val sut = createSUT()
        val denoisedPeaks = listOf(nextSpectralPeak(), nextSpectralPeak())
        every { noiseGate.apply(allPeaks) } returns denoisedPeaks

        val result = sut.analyze(audioFrame)

        assertEquals(denoisedPeaks, result.peaks)
    }

    // Flow
    @Test
    fun `initial analysis is empty`() {
        val sut = createSUT()

        assertEquals(SpectralAnalysis.EMPTY, sut.analysis.value)
    }

    @Test
    fun `when audio is analyzed, then the flow is updated`() {
        val sut = createSUT()

        val actual = sut.analyze(audioFrame)

        assertEquals(actual, sut.analysis.value)
    }

}