package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.entity.FPlayer
import com.github.onlaait.fbw.serializer.UuidAsStringSerializer
import com.github.onlaait.fbw.server.Logger
import com.github.onlaait.fbw.utils.*
import com.github.onlaait.fbw.utils.CoroutineManager.mustBeCompleted
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import net.minestom.server.entity.Player
import java.util.*
import kotlin.io.path.*

object BanSystem {

    private val PLAYER_PATH = Path("banned-players.json")
    private val IP_PATH = Path("banned-ips.json")

    val bannedPlayers = mutableSetOf<BannedPlayer>()
    val bannedIps = mutableSetOf<BannedIp>()

    init {
        load()
        storePlayers()
        storeIps()
    }

    fun load() {
        run {
            Logger.debug { "Loading banned players" }
            if (PLAYER_PATH.isRegularFile()) {
                try {
                    bannedPlayers.addAll(JSON.decodeFromString(PLAYER_PATH.readText()))
                } catch (e: IllegalArgumentException) {
                    Logger.error("Something is wrong with the format of '${PLAYER_PATH.name}', initializing it")
                }
            } else {
                PLAYER_PATH.writer().use { it.write("[]") }
            }
        }
        run {
            Logger.debug { "Loading banned ips" }
            if (IP_PATH.isRegularFile()) {
                try {
                    bannedIps.addAll(JSON.decodeFromString(IP_PATH.readText()))
                } catch (e: IllegalArgumentException) {
                    Logger.error("Something is wrong with the format of '${IP_PATH.name}', initializing it")
                }
            } else {
                IP_PATH.writer().use { it.write("[]") }
            }
        }
    }

    fun storePlayers() = CoroutineManager.FILE_OUT_SCOPE.launch {
        Logger.debug { "Storing banned players" }
        PLAYER_PATH.writer().use { it.write(cleanJson(JSON.encodeToString(bannedPlayers))) }
    }.mustBeCompleted()

    fun storeIps() = CoroutineManager.FILE_OUT_SCOPE.launch {
        Logger.debug { "Storing banned ips" }
        IP_PATH.writer().use { it.write(cleanJson(JSON.encodeToString(bannedIps))) }
    }.mustBeCompleted()

    @Serializable
    data class BannedPlayer(
        @Serializable(with = UuidAsStringSerializer::class)
        val uuid: UUID,
        val name: String,
        val reason: String?
    )

    @Serializable
    data class BannedIp(
        val ip: String,
        val reason: String?
    )

    fun Player.ban(reason: String? = null) {
        kick(getBanMessage(reason))
        bannedPlayers.add(BannedPlayer(uuid, username, reason))
        storePlayers()
    }

    fun banIp(ip: String, reason: String? = null): List<Player>? {
        if (bannedIps.any { it.ip == ip }) return null
        bannedIps.add(BannedIp(ip, reason))
        val targets = allPlayers.filter { it.ipAddress == ip }
        targets.forEach { player ->
            player.kick(getBanMessage(reason))
        }
        storeIps()
        return targets
    }

    fun pardon(username: String): Boolean {
        val removed = if (bannedPlayers.removeSingle { it.name == username }) {
            true
        } else {
            bannedPlayers.removeSingle { it.name.equals(username, ignoreCase = true) }
        }
        if (removed) storePlayers()
        return removed
    }

    fun pardon(uuid: UUID): Boolean {
        val removed = bannedPlayers.removeSingle { it.uuid == uuid }
        if (removed) storePlayers()
        return removed
    }

    fun pardonIp(ip: String): Boolean {
        val removed = bannedIps.removeSingle { it.ip == ip }
        if (removed) storeIps()
        return removed
    }

    fun Player.kickIfBanned(): Boolean {
        this as FPlayer
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

    private fun getBanMessage(reason: String?): TranslatableComponent =
        if (reason == null) {
            Component.translatable("multiplayer.disconnect.banned")
        } else {
            Component.translatable("multiplayer.disconnect.banned.reason", Component.text(reason))
        }
}