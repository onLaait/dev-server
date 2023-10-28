package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.serializer.UUIDAsStringSerializer
import com.github.laaitq.fbw.system.OpSystem.isOp
import com.github.laaitq.fbw.utils.IterableUtils.removeSingle
import com.github.laaitq.fbw.utils.JsonUtils
import com.github.laaitq.fbw.utils.MyCoroutines
import com.github.laaitq.fbw.utils.MyCoroutines.mustBeCompleted
import com.github.laaitq.fbw.utils.PlayerUtils.allPlayers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import java.util.*
import kotlin.io.path.*

object Whitelist {

    private const val filePath = "whitelist.json"
    val whitelistedPlayers = mutableSetOf<WhitelistedPlayer>()

    init {
        read()
        write()
    }

    fun read() {
        Logger.debug("Loading whitelist")
        val path = Path(filePath)
        if (path.isRegularFile()) {
            try {
                val list: Collection<WhitelistedPlayer> = JsonUtils.json.decodeFromString(path.reader().use { it.readText() })
                whitelistedPlayers.clear()
                whitelistedPlayers.addAll(list)
            } catch (e: Throwable) {
                Logger.error("Something is wrong with the format of '${path.name}', initializing it")
            }
        } else {
            path.writer().use { it.write("[]") }
        }
    }

    fun write() = MyCoroutines.fileOutputScope.launch {
        Logger.debug("Storing whitelist")
        Path(filePath).writer().use {
            it.write(JsonUtils.cleanJson(JsonUtils.json.encodeToString(whitelistedPlayers)))
        }
    }.mustBeCompleted()

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