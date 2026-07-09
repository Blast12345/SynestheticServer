package utilities

// TODO: Test me
class CachedProvider<K, V>(private val create: (K) -> V) {

    private var cached: Pair<K, V>? = null

    fun get(key: K): V {
        val current = cached
        if (current != null && current.first == key) {
            return current.second
        }
        val newValue = create(key)
        cached = Pair(key, newValue)
        return newValue
    }

}