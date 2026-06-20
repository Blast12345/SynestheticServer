import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    val kotlinVersion = "2.2.21"

    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion
    id("org.jetbrains.compose") version "1.8.2"
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    val coroutinesVersion = "1.11.0"
    val mockkVersion = "1.14.9"

    implementation("com.fazecast:jSerialComm:2.11.4")
    implementation("com.github.kwhat:jnativehook:2.2.2")
    implementation("com.github.wendykierp:JTransforms:3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${coroutinesVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:${coroutinesVersion}")
    implementation("tools.jackson.module:jackson-module-kotlin:3.1.+")
    implementation(compose.desktop.currentOs)

    // Unit Testing
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${coroutinesVersion}")
    testImplementation("io.mockk:mockk:${mockkVersion}")
    testImplementation(platform("org.junit:junit-bom:6.0.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.junit.jupiter:junit-jupiter-params")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Tests
tasks.test {
    useJUnitPlatform()
    jvmArgs("-Djunit.jupiter.extensions.autodetection.enabled=true")
}

// Compilation
// ENHANCEMENT: Automate package version
compose.desktop {
    application {
        mainClass = "MainKt"

        nativeDistributions {
            packageName = "SynestheticServer"
            packageVersion = "1.0.0"

            targetFormats(TargetFormat.Msi, TargetFormat.Dmg, TargetFormat.Deb)

            macOS {
                // Options for macOS
            }
            windows {
                // Options for Windows
            }
            linux {
                // Options for Linux
            }
        }
    }
}
