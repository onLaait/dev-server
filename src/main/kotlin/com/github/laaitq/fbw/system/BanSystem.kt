package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.serializer.UUIDAsStringSerializer
import com.github.laaitq.fbw.utils.IterableUtils.removeSingle
import com.github.laaitq.fbw.utils.JsonUtils
import com.github.laaitq.fbw.utils.PlayerUtils
import com.github.laaitq.fbw.utils.PlayerUtils.ipAddress
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.minestom.server.entity.Player
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

object BanSystem {
    private const val playersFilePath = "banned-players.json"
    private const val ipsFilePath = "banned-ips.json"
    val bannedPlayers = mutableSetOf<BannedPlayer>()
    val bannedIps = mutableSetOf<BannedIp>()

    init {
        read()
        write()
    }

    fun read() {
        Logger.debug("Loading banned players and ips")
        if (File(playersFilePath).exists()) {
            try {
                bannedPlayers.addAll(JsonUtils.json.decodeFromString(FileReader(playersFilePath).use { it.readText() }))
            } catch (e: IllegalArgumentException) {
                Logger.error("Something is wrong with the format of '$playersFilePath', initializing it")
            }
        } else {
            BufferedWriter(FileWriter(playersFilePath)).use {
                it.write("[]")
            }
        }

        if (File(ipsFilePath).exists()) {
            try {
                bannedIps.addAll(JsonUtils.json.decodeFromString(FileReader(ipsFilePath).use { it.readText() }))
            } catch (e: IllegalArgumentException) {
                Logger.error("Something is wrong with the format of '$ipsFilePath', initializing it")
            }
        } else {
            BufferedWriter(FileWriter(ipsFilePath)).use {
                it.write("[]")
            }
        }
    }

    fun write() {
        Logger.debug("Storing banned players and ips")
        BufferedWriter(FileWriter(playersFilePath)).use {
            it.write(JsonUtils.cleanJson(JsonUtils.json.encodeToString(bannedPlayers)))
        }

        BufferedWriter(FileWriter(ipsFilePath)).use {
            it.write(JsonUtils.cleanJson(JsonUtils.json.encodeToString(bannedIps)))
        }
    }

    @Serializable
    data class BannedPlayer(
        @Serializable(with = UUIDAsStringSerializer::class)
        val uuid: UUID,
        val name: String,
        val reason: String?
    )

    @Serializable
    data class BannedIp(
        val ip: String,
        val reason: String?
    )

    fun Player.ban(reason: String?) {
        kick(getBanMessage(reason))
        bannedPlayers.add(BannedPlayer(uuid, username, reason))
        write()
    }

    fun banIp(ip: String, reason: String?): List<Player>? {
        if (bannedIps.find { it.ip == ip } != null) return null
        bannedIps.add(BannedIp(ip, reason))
        val targets = PlayerUtils.allPlayers.filter { it.ipAddress == ip }
        targets.forEach { player ->
            player.kick(getBanMessage(reason))
        }
        write()
        return targets
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

    fun pardonIp(ip: String): Boolean {
        val removed = bannedIps.removeSingle { it.ip == ip }
        if (removed) write()
        return removed
    }

    fun Player.kickIfBanned(): Boolean {
        for (e in bannedPlayers) {
            if (e.uuid == uuid) {
                kick(getBanMessage(e.reason))
                return true
            }
        }
        for (e in bannedIps) {
            if (e.ip == ipAddress) {
                kick(getBanMessage(e.reason))
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