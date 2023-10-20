package com.github.laaitq.fbw.utils

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player

object PlayerUtils {

    val allPlayers: Collection<Player>
        get() = MinecraftServer.getConnectionManager().onlinePlayers

    val onlinePlayersCount: Int
        get() = MinecraftServer.getConnectionManager().onlinePlayers.size
}