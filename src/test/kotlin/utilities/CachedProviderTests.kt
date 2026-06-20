package utilities

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CachedProviderTests {

    @Test
    fun `creates value on first access`() {
        val provider = CachedProvider { key: String -> key.uppercase() }

        val result = provider.get("hello")

        assertEquals("HELLO", result)
    }

    @Test
    fun `returns cached value for same key`() {
        var createCount = 0

        val provider = CachedProvider { key: String ->
            createCount++
            key.uppercase()
        }

        provider.get("hello")
        provider.get("hello")

        assertEquals(1, createCount)
    }

    @Test
    fun `recreates value when key changes`() {
        var createCount = 0

        val provider = CachedProvider { key: String ->
            createCount++
            key.uppercase()
        }

        val first = provider.get("hello")
        val second = provider.get("world")

        assertEquals("HELLO", first)
        assertEquals("WORLD", second)
        assertEquals(2, createCount)
    }
    
}