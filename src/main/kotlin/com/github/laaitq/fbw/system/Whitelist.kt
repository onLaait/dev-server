package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.serializer.UUIDAsStringSerializer
import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.utils.IterableUtils.removeSingle
import com.github.laaitq.fbw.utils.JsonUtils
import com.github.laaitq.fbw.utils.PlayerUtils.allPlayers
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

object Whitelist {

    private const val jsonPath = "whitelist.json"
    val whitelistedPlayers = mutableSetOf<WhitelistedPlayer>()

    init {
        read()
        write()
    }

    fun read() {
        if (File(jsonPath).exists()) {
            try {
                val list: Collection<WhitelistedPlayer> = Json.decodeFromString(FileReader(jsonPath).readText())
                whitelistedPlayers.clear()
                whitelistedPlayers.addAll(list)
            } catch (e: Throwable) {
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
            it.write(JsonUtils.cleanJson(JsonUtils.prettyJson.encodeToString(whitelistedPlayers)))
        }
    }

    fun enable() {
        if (ServerProperties.WHITE_LIST) return
        ServerProperties.WHITE_LIST = true
        if (ServerProperties.ENFORCE_WHITELIST) {
            allPlayers.forEach { player ->
                player.kickIfNotWhitelisted()
            }
        }
    }

    fun disable() {
        ServerProperties.WHITE_LIST = false
    }

    fun add(uuid: UUID, name: String): Boolean {
        if (whitelistedPlayers.find { it.uuid == uuid } != null) return false
        val added = whitelistedPlayers.add(WhitelistedPlayer(uuid, name))
        if (added) write()
        return added
    }

    fun add(player: Player): Boolean = add(player.uuid, player.username)

    fun remove(uuid: UUID): Boolean {
        val removed = whitelistedPlayers.removeSingle { it.uuid == uuid }
        if (removed) {
            write()
            if (ServerProperties.WHITE_LIST && ServerProperties.ENFORCE_WHITELIST) {
                allPlayers.find { it.uuid == uuid }?.kickIfNotWhitelisted()
            }
        }
        return removed
    }

    fun remove(name: String): Boolean {
        val find = whitelistedPlayers.find { it.name == name }
            ?: whitelistedPlayers.find { it.name.equals(name, ignoreCase = true) }
            ?: return false
        whitelistedPlayers.remove(find)
        write()
        if (ServerProperties.WHITE_LIST && ServerProperties.ENFORCE_WHITELIST) {
            allPlayers.find { it.uuid == find.uuid }?.kickIfNotWhitelisted()
        }
        return true
    }

    fun remove(player: Player): Boolean = remove(player.uuid)
    
    val Player.isWhitelisted: Boolean
        get() = (whitelistedPlayers.find { it.uuid == this.uuid } != null)

    fun Player.kickIfNotWhitelisted(): Boolean {
        if (!this.isOp && !this.isWhitelisted) {
            kick(Component.translatable("multiplayer.disconnect.not_whitelisted"))
            Logger.info("Disconnecting $username (${playerConnection.remoteAddress}): You are not white-listed on this server!")
            return true
        }
        return false
    }

    @Serializable
    data class WhitelistedPlayer(
        @Serializable(with = UUIDAsStringSerializer::class)
        val uuid: UUID,
        val name: String
    )
}