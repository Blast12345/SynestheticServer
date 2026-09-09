package wrappers.operatingSystem

sealed interface OperatingSystem

object MacOS : OperatingSystem

object Windows : OperatingSystem

data class Linux(val desktop: LinuxDesktop) : OperatingSystem
enum class LinuxDisplayServer { X11, WAYLAND, NONE }