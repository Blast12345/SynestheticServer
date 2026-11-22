package keyBinding


class Keymap<C>(vararg bindings: Pair<Set<Key>, C>) {
    private val bindingsByKeys = bindings.toMap()

    fun commandFor(heldKeys: Set<Key>): C? = bindingsByKeys[heldKeys]
}

operator fun Key.plus(other: Key): Set<Key> = setOf(this, other)
operator fun Set<Key>.plus(other: Key): Set<Key> = this + setOf(other)