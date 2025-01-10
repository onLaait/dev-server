package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.entity.FPlayer
import net.minestom.server.MinecraftServer

val allPlayers: List<FPlayer>
    get() = MinecraftServer.getConnectionManager().onlinePlayers.map { it as FPlayer }

val allPlayersCount: Int
    get() = MinecraftServer.getConnectionManager().onlinePlayers.size
