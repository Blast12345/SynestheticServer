package logging

enum class LogLevel { ERROR, WARNING, SUCCESS, DEBUG }

object Logger {

    var level: LogLevel = LogLevel.ERROR

    fun error(message: String) {
        println("${LogColor.Red.code}ERROR: $message${LogColor.Default.code}")
    }

    fun error(message: String, throwable: Throwable) {
        error("$message | Exception: ${throwable.message}")
    }

    fun warning(message: String) {
        if (level >= LogLevel.WARNING) println("${LogColor.Orange.code}WARNING: $message${LogColor.Default.code}")
    }

    fun success(message: String) {
        if (level >= LogLevel.SUCCESS) println("${LogColor.Green.code}SUCCESS: $message${LogColor.Default.code}")
    }

    fun debug(message: String) {
        if (level >= LogLevel.DEBUG) println("${LogColor.Yellow.code}DEBUG: $message${LogColor.Yellow.code}")
    }

}