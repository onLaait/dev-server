package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.serializer.UUIDAsStringSerializer
import com.github.laaitq.fbw.utils.IterableUtils.removeSingle
import com.github.laaitq.fbw.utils.JsonUtils
import com.github.laaitq.fbw.utils.PlayerUtils
import com.github.laaitq.fbw.utils.PlayerUtils.ipAddress
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.minestom.server.entity.Player
import java.io.File
import java.util.*

object BanSystem {
    private const val playersFilePath = "banned-players.json"
    private const val ipsFilePath = "banned-ips.json"
    val bannedPlayers = mutableSetOf<BannedPlayer>()
    val bannedIps = mutableSetOf<BannedIp>()

    init {
        read()
        writePlayers()
        writeIps()
    }

    fun read() {
        run {
            Logger.debug("Loading banned players")
            val file = File(playersFilePath)
            if (file.isFile) {
                try {
                    bannedPlayers.addAll(JsonUtils.json.decodeFromString(file.reader().use { it.readText() }))
                } catch (e: IllegalArgumentException) {
                    Logger.error("Something is wrong with the format of '${file.path}', initializing it")
                }
            } else {
                file.writer().use { it.write("[]") }
            }
        }
        run {
            Logger.debug("Loading banned ips")
            val file = File(ipsFilePath)
            if (file.isFile) {
                try {
                    bannedIps.addAll(JsonUtils.json.decodeFromString(file.reader().use { it.readText() }))
                } catch (e: IllegalArgumentException) {
                    Logger.error("Something is wrong with the format of '${file.path}', initializing it")
                }
            } else {
                file.writer().use { it.write("[]") }
            }
        }
    }

    fun writePlayers() {
        Logger.debug("Storing banned players")
        File(playersFilePath).writer().use {
            it.write(JsonUtils.cleanJson(JsonUtils.json.encodeToString(bannedPlayers)))
        }
    }

    fun writeIps() {
        Logger.debug("Storing banned ips")
        File(ipsFilePath).writer().use {
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
        writePlayers()
    }

    fun banIp(ip: String, reason: String?): List<Player>? {
        if (bannedIps.find { it.ip == ip } != null) return null
        bannedIps.add(BannedIp(ip, reason))
        val targets = PlayerUtils.allPlayers.filter { it.ipAddress == ip }
        targets.forEach { player ->
            player.kick(getBanMessage(reason))
        }
        writeIps()
        return targets
    }

    fun pardon(username: String): Boolean {
        val removed = if (bannedPlayers.removeSingle { it.name == username }) {
            true
        } else {
            bannedPlayers.removeSingle { it.name.equals(username, ignoreCase = true) }
        }
        if (removed) writePlayers()
        return removed
    }

    fun pardon(uuid: UUID): Boolean {
        val removed = bannedPlayers.removeSingle { it.uuid == uuid }
        if (removed) writePlayers()
        return removed
    }

    fun pardonIp(ip: String): Boolean {
        val removed = bannedIps.removeSingle { it.ip == ip }
        if (removed) writeIps()
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