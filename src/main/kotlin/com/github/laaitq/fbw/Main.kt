package com.github.laaitq.fbw

import com.github.laaitq.fbw.command.CommandRegister
import com.github.laaitq.fbw.system.*
import com.github.laaitq.fbw.terminal.MinestomTerminal
import com.github.laaitq.fbw.utils.ComponentUtils
import com.github.laaitq.fbw.utils.PlayerUtils
import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.extras.MojangAuth

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

        ServerProperties
        val viewDistanceStr = ServerProperties.VIEW_DISTANCE.toString()
        System.setProperty("minestom.chunk-view-distance", viewDistanceStr)
        System.setProperty("minestom.entity-view-distance", viewDistanceStr)
        System.setProperty("minestom.tps", "100")

        val minecraftServer = MinecraftServer.init()
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

        val ip = ServerProperties.SERVER_IP
        val port = ServerProperties.SERVER_PORT
        Logger.info("Starting Minecraft server on ${ip.ifBlank { "*" }}:$port")
        minecraftServer.start(ip.ifBlank { "0.0.0.0" }, port)

        PlayerData
        BanSystem
        OpSystem
        Whitelist

        Event
        CommandRegister

        ComponentUtils

        MinecraftServer.getSchedulerManager().buildShutdownTask {
            Logger.info("Stopping server")
            val closeMessage = Component.translatable("multiplayer.disconnect.server_shutdown")
            PlayerUtils.allPlayers.forEach { player ->
                player.kick(closeMessage)
            }
            MinestomTerminal.stop()
            Thread.sleep(100)
        }

        Instance

        MinestomTerminal.start()

        Logger.info("Done (${(System.currentTimeMillis()-startTime)/1000.0}s)! For help, type \"help\"")
    }
}