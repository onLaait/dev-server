plugins {
    val kotlinVersion = "1.9.25"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.serialization") version kotlinVersion

    id("java")
    id("com.gradleup.shadow") version "8.3.3"
}

group = "com.github.onlaait.fbw"
version = "0.1"

repositories {
    mavenCentral()
//    maven("https://jitpack.io")
//    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/sonatype-oss-snapshots1")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("net.minestom:minestom-snapshots:d8c5831f4e")

    val log4jVersion = "2.23.1"
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-iostreams:$log4jVersion")

    implementation("net.minecrell:terminalconsoleappender:1.3.0")
    implementation("org.jline:jline-terminal-jansi:3.20.0")

    val adventureVersion = "4.17.0"
    implementation("net.kyori:adventure-api:$adventureVersion")
    implementation("net.kyori:adventure-text-minimessage:$adventureVersion")
    implementation("net.kyori:adventure-text-serializer-ansi:$adventureVersion")

    implementation("org.joml:joml:1.10.5")

    implementation("com.charleskorn.kaml:kaml:0.59.0")

    val physxVersion = "2.4.0"
    implementation("de.fabmax:physx-jni:$physxVersion")
    runtimeOnly("de.fabmax:physx-jni:$physxVersion:natives-windows")
    runtimeOnly("de.fabmax:physx-jni:$physxVersion:natives-linux")
    runtimeOnly("de.fabmax:physx-jni:$physxVersion:natives-macos")
    runtimeOnly("de.fabmax:physx-jni:$physxVersion:natives-macos-arm64")

    implementation(platform("org.lwjgl:lwjgl-bom:3.3.4"))
    implementation("org.lwjgl", "lwjgl")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-windows")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-linux")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-macos")
    runtimeOnly("org.lwjgl", "lwjgl", classifier = "natives-macos-arm64")

//    implementation("de.fabmax.kool:kool-physics:0.13.0")
}

tasks {
    build {
        dependsOn(shadowJar)
    }

    shadowJar {
        archiveBaseName.set("fbw")
        archiveClassifier.set("")
        mergeServiceFiles()

        manifest {
            attributes (
                "Main-Class" to "${project.group}.Main",
                "Multi-Release" to true
            )
        }

        transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
    }
}

kotlin {
    jvmToolchain(21)
}