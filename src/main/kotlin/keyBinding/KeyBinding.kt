package keyBinding

data class KeyBinding<C>(
    val keys: Set<Key>,
    val command: C
)