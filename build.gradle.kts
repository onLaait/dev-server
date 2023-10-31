plugins {
    kotlin("jvm") version "1.9.10"
    kotlin("plugin.serialization") version "1.9.10"
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.github.laaitq.fbw"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/sonatype-oss-snapshots1")
}

dependencies {
    implementation("dev.hollowcube:minestom-ce:438338381e")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.21.1")
    implementation("org.apache.logging.log4j:log4j-core:2.21.1")
    implementation("org.apache.logging.log4j:log4j-api-kotlin:1.3.0")
    implementation("org.jline:jline:3.21.0")
    implementation("org.jline:jline-terminal-jansi:3.21.0")
    implementation("net.kyori:adventure-api:4.13.0")
    implementation("net.kyori:adventure-text-minimessage:4.13.0")
    implementation("net.kyori:adventure-text-serializer-ansi:4.14.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("com.charleskorn.kaml:kaml:0.55.0")
    implementation("org.joml:joml:1.10.5")
}

tasks {
    shadowJar {
        archiveBaseName.set("fbw")
        archiveClassifier.set("")
        mergeServiceFiles()

        manifest {
            attributes (
                "Main-Class" to "com.github.laaitq.fbw.Main",
                "Multi-Release" to true
            )
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}