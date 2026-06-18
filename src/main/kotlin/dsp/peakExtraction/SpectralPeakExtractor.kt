package dsp.peakExtraction

// NOTE: We don't know exactly what metadata any extraction algorithm might need,
// so we can't enforce a particular interface. This interface is simply for exhaustive when statements
sealed interface SpectralPeakExtractor