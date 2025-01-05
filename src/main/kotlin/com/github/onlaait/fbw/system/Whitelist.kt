package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.server.Logger
import com.github.onlaait.fbw.system.OpSystem.isOp
import com.github.onlaait.fbw.utils.*
import com.github.onlaait.fbw.utils.CoroutineManager.mustBeCompleted
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import java.util.*
import kotlin.io.path.*

object Whitelist {

    private val PATH = Path("whitelist.json")

    val whitelistedPlayers = mutableSetOf<UuidAndName>()

    init {
        load()
        store()
    }

    fun load() {
        Logger.debug { "Loading whitelist" }
        if (PATH.isRegularFile()) {
            try {
                val list: Set<UuidAndName> = JSON.decodeFromString(PATH.readText())
                whitelistedPlayers.clear()
                whitelistedPlayers.addAll(list)
            } catch (e: Throwable) {
                Logger.error("Something is wrong with the format of '${PATH.name}', initializing it")
            }
        } else {
            PATH.writer().use { it.write("[]") }
        }
    }

    fun store() = CoroutineManager.FILE_OUT_SCOPE.launch {
        Logger.debug { "Storing whitelist" }
        PATH.writer().use {
            it.write(cleanJson(JSON.encodeToString(whitelistedPlayers)))
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
        if (whitelistedPlayers.any { it.uuid == uuid }) return false
        val added = whitelistedPlayers.add(UuidAndName(uuid, name))
        if (added) store()
        return added
    }

    fun add(player: Player): Boolean = add(player.uuid, player.username)

    fun remove(uuid: UUID): Boolean {
        val removed = whitelistedPlayers.removeSingle { it.uuid == uuid }
        if (removed) {
            store()
            if (ServerProperties.run { WHITE_LIST && ENFORCE_WHITELIST }) {
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
        store()
        if (ServerProperties.WHITE_LIST && ServerProperties.ENFORCE_WHITELIST) {
            allPlayers.find { it.uuid == find.uuid }?.kickIfNotWhitelisted()
        }
        return true
    }

    fun remove(player: Player): Boolean = remove(player.uuid)
    
    val Player.isWhitelisted: Boolean
        get() = (whitelistedPlayers.any { it.uuid == uuid })

    fun Player.kickIfNotWhitelisted(): Boolean {
        if (isOp || isWhitelisted) return false
        kick(Component.translatable("multiplayer.disconnect.not_whitelisted"))
        return true
    }
}