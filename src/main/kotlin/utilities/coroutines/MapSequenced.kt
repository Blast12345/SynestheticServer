package utilities.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T, R> Flow<Sequenced<T>>.mapSequenced(
    gapDetector: SequenceGapDetector = SequenceGapDetector(),
    onGap: (size: Long) -> Unit = {},
    transform: suspend (T) -> R
): Flow<Sequenced<R>> {
    var outgoingNumber = 0L

    return map { incoming ->
        val gap = gapDetector.check(incoming.sequenceNumber)

        if (gap != 0L) {
            onGap(gap)
            // TODO: Should we update the outgoing number to correspond to the next expected number?
        }

        Sequenced(outgoingNumber++, transform(incoming.value))
    }
}