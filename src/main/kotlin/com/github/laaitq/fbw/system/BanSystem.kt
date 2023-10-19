package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.serializer.UUIDAsStringSerializer
import com.github.laaitq.fbw.utils.IterableUtils.removeSingle
import com.github.laaitq.fbw.utils.JsonUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.minestom.server.entity.Player
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

object BanSystem {
    private const val jsonPath = "banned-players.json"
    val bannedPlayers = mutableSetOf<BannedPlayer>()

    init {
        read()
        write()
    }

    fun read() {
        if (File(jsonPath).exists()) {
            try {
                bannedPlayers.addAll(Json.decodeFromString(FileReader(jsonPath).readText()))
            } catch (e: IllegalArgumentException) {
                Logger.error("Something is wrong with the format of '$jsonPath', initializing it")
            }
        } else {
            BufferedWriter(FileWriter(jsonPath)).use {
                it.write("[]")
            }
        }
    }

    fun write() {
        BufferedWriter(FileWriter(jsonPath)).use {
            it.write(JsonUtils.cleanJson(JsonUtils.prettyJson.encodeToString(bannedPlayers)))
        }
    }

    @Serializable
    data class BannedPlayer(
        @Serializable(with = UUIDAsStringSerializer::class)
        val uuid: UUID,
        val name: String,
        val reason: String?
    )

    fun Player.ban() {
        kick(getBanMessage(null))
        bannedPlayers.add(BannedPlayer(uuid, username, null))
        write()
    }

    fun Player.ban(reason: String?) {
        kick(getBanMessage(reason))
        bannedPlayers.add(BannedPlayer(uuid, username, reason))
        write()
    }

    fun pardon(username: String): Boolean {
        val removed = if (bannedPlayers.removeSingle { it.name == username }) {
            true
        } else {
            bannedPlayers.removeSingle { it.name.equals(username, ignoreCase = true) }
        }
        if (removed) write()
        return removed
    }

    fun pardon(uuid: UUID): Boolean {
        val removed = bannedPlayers.removeSingle { it.uuid == uuid }
        if (removed) write()
        return removed
    }

    fun Player.kickIfBanned(): Boolean {
        for (e in bannedPlayers) {
            if (e.uuid == uuid) {
                kick(getBanMessage(e.reason))
                Logger.info("Disconnecting $username (${playerConnection.remoteAddress}): " +
                        if (e.reason == null) {
                            "You are banned from this server"
                        } else {
                            "You are banned from this server.\nReason: ${e.reason}"
                        }
                )
                return true
            }
        }
        return false
    }

    private fun getBanMessage(reason: String?): TranslatableComponent {
        return if (reason == null) {
            Component.translatable("multiplayer.disconnect.banned")
        } else {
            Component.translatable("multiplayer.disconnect.banned.reason", Component.text(reason))
        }
    }
}