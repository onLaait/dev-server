plugins {
    kotlin("jvm") version "1.9.21"
    kotlin("plugin.serialization") version "1.9.21"
    id("java")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}
group = "com.github.onlaait.fbw"
version = "0.1"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/sonatype-oss-snapshots1")
}

dependencies {
    implementation("dev.hollowcube:minestom-ce:1554487748")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    val log4jVersion = "2.22.0"
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-core:$log4jVersion")
    implementation("org.apache.logging.log4j:log4j-iostreams:$log4jVersion")

    implementation("net.minecrell:terminalconsoleappender:1.3.0")
    implementation("org.jline:jline-terminal-jansi:3.20.0")

    val adventureVersion = "4.14.0"
    implementation("net.kyori:adventure-api:$adventureVersion")
    implementation("net.kyori:adventure-text-minimessage:$adventureVersion")
    implementation("net.kyori:adventure-text-serializer-ansi:$adventureVersion")

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
                "Main-Class" to "${project.group}.Main",
                "Multi-Release" to true
            )
        }

        transform(com.github.jengelman.gradle.plugins.shadow.transformers.Log4j2PluginsCacheFileTransformer::class.java)
    }

    build {
        dependsOn(shadowJar)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}