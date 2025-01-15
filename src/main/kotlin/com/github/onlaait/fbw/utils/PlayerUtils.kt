package com.github.onlaait.fbw.utils

import com.github.onlaait.fbw.entity.FPlayer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.PlayerSkin
import java.util.*

val allPlayers: List<FPlayer>
    get() = MinecraftServer.getConnectionManager().onlinePlayers.map { it as FPlayer }

val allPlayersCount: Int
    get() = MinecraftServer.getConnectionManager().onlinePlayers.size

fun PlayerSkin.isSlim(): Boolean {
    val textures = Base64.getDecoder().decode(textures()).decodeToString()
    val metadata = Json.parseToJsonElement(textures)
        .jsonObject["textures"]!!
        .jsonObject["SKIN"]!!
        .jsonObject["metadata"] ?: return false
    val model = metadata
        .jsonObject["model"]!!
        .jsonPrimitive.content
    return model == "slim"
}