package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.server.PlayerP
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.net.InetSocketAddress

object PlayerUtils {

    val allPlayers: Collection<Player>
        get() = MinecraftServer.getConnectionManager().onlinePlayers

    val onlinePlayersCount: Int
        get() = MinecraftServer.getConnectionManager().onlinePlayers.size

    val Player.data
        get() = (this as PlayerP).data

    var Player.brand
        get() = (this as PlayerP).brand
        set(value) {
            (this as PlayerP).brand = value
        }

    val Player.ipAddress: String
        get() = (this.playerConnection.remoteAddress as InetSocketAddress).address.hostAddress
}