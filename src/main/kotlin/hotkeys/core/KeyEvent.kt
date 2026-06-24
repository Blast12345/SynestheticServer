package hotkeys.core

data class KeyEvent(val key: Key, val modifiers: Set<Modifier>) {
    fun hasModifiers(vararg required: Modifier): Boolean =
        modifiers.containsAll(required.toSet())
}

enum class Key { PAGE_UP, PAGE_DOWN, HOME, END }
enum class Modifier { CTRL, SHIFT }