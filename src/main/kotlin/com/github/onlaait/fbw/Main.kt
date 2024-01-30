package com.github.onlaait.fbw

import com.github.onlaait.fbw.command.CommandRegister
import com.github.onlaait.fbw.physx.PxManager
import com.github.onlaait.fbw.server.*
import com.github.onlaait.fbw.system.*
import com.github.onlaait.fbw.utils.ComponentUtils
import com.github.onlaait.fbw.utils.MyCoroutines
import com.github.onlaait.fbw.utils.PlayerUtils
import com.github.onlaait.fbw.utils.ServerUtils
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.runBlocking
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.MojangAuth
import net.minestom.server.thread.MinestomThread
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.core.config.Configurator
import org.apache.logging.log4j.io.IoBuilder
import org.lwjgl.system.Configuration
import org.lwjgl.system.Platform
import java.text.DecimalFormat

object Main {

    @JvmStatic
    fun main(args: Array<String>) {
        println("Starting ${javaClass.name}")
        println("System Info: Java ${Runtime.version().feature()} (${System.getProperty("java.vm.name")} ${Runtime.version()}) Host: ${System.getProperty("os.name")} ${System.getProperty("os.version")} (${System.getProperty("os.arch")})")

        runServer()
    }

    private fun runServer() {
        val startTime = System.currentTimeMillis()
        Logger.info("Starting Minecraft server version ${MinecraftServer.VERSION_NAME}")

        System.setProperty("minestom.tps", "100")
        System.setProperty("joml.fastmath", "true")
        System.setProperty("joml.sinLookup", "true")

        System.setOut(IoBuilder.forLogger(Logger.default).setLevel(Level.INFO).buildPrintStream())
        System.setErr(IoBuilder.forLogger(Logger.default).setLevel(Level.ERROR).buildPrintStream())

        Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler)

        val platform = when (Platform.get()) {
            Platform.WINDOWS -> "windows"
            Platform.LINUX -> "linux"
            Platform.MACOSX -> if (Platform.getArchitecture() == Platform.Architecture.ARM64) "macos-arm64" else "macos"
            else -> {
                Logger.error("System platform not supported!")
                return
            }
        }
        Configuration.LIBRARY_PATH.set(ClassLoader.getSystemResource("lwjgl/$platform").path.removePrefix("/"))

        ServerProperties
        if (ServerProperties.DEBUG) {
            Configurator.setLevel("DefaultLogger", Level.DEBUG)
            Configurator.setLevel("ConsoleLogger", Level.DEBUG)
            Configurator.setLevel("FileLogger", Level.DEBUG)
        }
        val viewDistanceStr = ServerProperties.VIEW_DISTANCE.toString()
        System.setProperty("minestom.chunk-view-distance", viewDistanceStr)
        System.setProperty("minestom.entity-view-distance", viewDistanceStr)
//        VelocityProxy.enable("9gXnEEDghmNr")

        MinestomThread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler)

        val minecraftServer = MinecraftServer.init()

        MinecraftServer.getSchedulerManager().buildShutdownTask {
            Logger.info("Stopping server")

            Terminal.stop()
            PlayerData.writeAllPlayers()

            val closeMessage = Component.translatable("multiplayer.disconnect.server_shutdown")
            PlayerUtils.allPlayers.forEach { player ->
                player.kick(closeMessage)
            }

            runBlocking { MyCoroutines.mustJobs.joinAll() }

            Thread.sleep(100)
        }

        MojangAuth.init()

        MinecraftServer.setCompressionThreshold(
            ServerProperties.NETWORK_COMPRESSION_THRESHOLD.let {
                when (it) {
                    -1 -> 0 // Minestom에선 0이 비활성화
                    in 0..63 -> 64 // The Ethernet spec requires that packets less than 64 bytes become padded to 64 bytes. Thus, setting a value lower than 64 may not be beneficial.
                    else -> it
                }
            }
        )

        MinecraftServer.getConnectionManager().setPlayerProvider(::PlayerP)

        ServerUtils
        ComponentUtils
        MyCoroutines

        PlayerData
        BanSystem
        MuteSystem
        OpSystem
        Whitelist
        if (ServerProperties.ENABLE_KAKC) Kakc

        Event
        CommandRegister

        Instance

        PxManager

        val ip = ServerProperties.SERVER_IP
        val port = ServerProperties.SERVER_PORT
        Logger.info("Hosting Minecraft server on ${ip.ifBlank { "*" }}:$port")
        try {
            minecraftServer.start(ip.ifBlank { "0.0.0.0" }, port)
        } catch (e: Exception) {
            MinecraftServer.stopCleanly()
            return
        }

        Terminal.start()

        val bootTime = (System.currentTimeMillis() - startTime) / 1000.0
        Logger.info("Done (${DecimalFormat("#.##").format(bootTime)}s)! For help, type \"help\"")
    }
}