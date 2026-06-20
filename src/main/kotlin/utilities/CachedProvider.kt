package utilities

class CachedProvider<K, V>(private val create: (K) -> V) {

    private var key: K? = null
    private var value: V? = null

    fun get(key: K): V {
        if (key != this.key) {
            this.key = key
            value = create(key)
        }
        return value!!
    }

}