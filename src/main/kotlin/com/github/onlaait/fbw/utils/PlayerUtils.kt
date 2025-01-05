package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.server.FPlayer
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import java.net.InetSocketAddress

val allPlayers: Collection<Player>
    get() = MinecraftServer.getConnectionManager().onlinePlayers

val allPlayersCount: Int
    get() = allPlayers.size

val Player.data
    get() = (this as FPlayer).data

var Player.brand
    get() = (this as FPlayer).brand
    set(value) {
        (this as FPlayer).brand = value
    }

val Player.ipAddress: String
    get() = (playerConnection.remoteAddress as InetSocketAddress).address.hostAddress
