package utilities.coroutines

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

fun <T> Flow<Sequenced<T>>.onEachSequenced(
    gapDetector: SequenceGapDetector = SequenceGapDetector(),
    onGap: (size: Long) -> Unit = {},
    onReset: () -> Unit = {},
    action: suspend (T) -> Unit
): Flow<Sequenced<T>> {
    return onEach { incoming ->
        when (val result = gapDetector.check(incoming.sequenceNumber)) {
            is SequenceCheck.Ok -> {}
            is SequenceCheck.Gap -> onGap(result.size)
            is SequenceCheck.Reset -> onReset()
        }

        action(incoming.value)
    }
}