package utilities.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun <T, R> Flow<Sequenced<T>>.mapSequenced(
    gapDetector: SequenceGapDetector = SequenceGapDetector(),
    onGap: (size: Long) -> Unit = {},
    onReset: () -> Unit = {},
    transform: suspend (T) -> R
): Flow<Sequenced<R>> {
    var outgoingNumber = 0L

    return map { incoming ->
        when (val result = gapDetector.check(incoming.sequenceNumber)) {
            is SequenceCheck.Ok -> {}
            is SequenceCheck.Gap -> onGap(result.size)
            is SequenceCheck.Reset -> onReset()
        }

        Sequenced(outgoingNumber++, transform(incoming.value))
    }
}