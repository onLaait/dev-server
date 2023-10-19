package com.github.laaitq.fbw.utils

import com.github.laaitq.fbw.system.ServerProperties
import com.github.laaitq.fbw.system.ServerStatus
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

object PlayerUtils {

    val allPlayers: Collection<Player>
        get() = MinecraftServer.getConnectionManager().onlinePlayers

    val onlinePlayersCount: Int
        get() = MinecraftServer.getConnectionManager().onlinePlayers.size

    fun Player.sendTabList() {
        val memory = ServerStatus.Memory
        this.sendPlayerListHeaderAndFooter(
            TextUtils.formatText(
                "                                   \n" +
                "테스트 서버\n" +
                "${onlinePlayersCount}/${ServerProperties.MAX_PLAYERS}\n"
            ), TextUtils.formatText(
                "\n" +
                "<gray>TPS: <white>${String.format("%.1f", ServerStatus.TPS.tps)} <dark_gray>| <gray>Memory: <white>${memory.usedMem}<gray>/${memory.totalMem} MB\n" +
                "<gray>Ping: <white>${this.latency} <gray>ms\n"
            )
        )
    }
}