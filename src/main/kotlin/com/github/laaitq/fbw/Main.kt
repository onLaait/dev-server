package com.github.laaitq.fbw

import com.github.laaitq.fbw.command.CommandRegister
import com.github.laaitq.fbw.system.*
import com.github.laaitq.fbw.terminal.MinestomTerminal
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
        Logger.info("Starting minecraft server version ${MinecraftServer.VERSION_NAME}")

        System.setProperty("minestom.tps", "40")
        val minecraftServer = MinecraftServer.init()

        MojangAuth.init()

        ServerProperties
        PlayerData
        BanSystem
        OpSystem
        Whitelist
        Listener
        CommandRegister

        MinecraftServer.setCompressionThreshold(
            ServerProperties.NETWORK_COMPRESSION_THRESHOLD.let {
                when (it) {
                    -1 -> 0 // Minestom에선 0이 비활성화
                    in 0..63 -> 64 // The Ethernet spec requires that packets less than 64 bytes become padded to 64 bytes. Thus, setting a value lower than 64 may not be beneficial.
                    else -> it
                }
            }
        )

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

        Logger.info("Hosting server on ${ServerProperties.SERVER_IP.ifBlank { "*" }}:${ServerProperties.SERVER_PORT}")
        minecraftServer.start(ServerProperties.SERVER_IP, ServerProperties.SERVER_PORT)

        MinestomTerminal.start()

        Logger.info("Done (${(System.currentTimeMillis()-startTime)/1000.0}s)! For help, type \"help\"")
    }
}