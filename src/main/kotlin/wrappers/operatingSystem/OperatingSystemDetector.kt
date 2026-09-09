package wrappers.operatingSystem

fun detectOperatingSystem(): OperatingSystem {
    val os = System.getProperty("os.name").lowercase()

    return when {
        "mac" in os -> MacOS
        "windows" in os -> Windows
        "linux" in os -> Linux(detectLinuxDesktop())
        else -> throw UnsupportedOperationException("Unsupported OS: $os")
    }
}

private fun detectLinuxDesktop(): LinuxDesktop = when {
    System.getenv("DISPLAY") != null -> LinuxDesktop.X11
    System.getenv("WAYLAND_DISPLAY") != null -> LinuxDesktop.WAYLAND
    else -> LinuxDesktop.HEADLESS
}