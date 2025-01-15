package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.server.Logger
import com.github.onlaait.fbw.system.Whitelist.kickIfNotWhitelisted
import com.github.onlaait.fbw.utils.CoroutineManager
import com.github.onlaait.fbw.utils.CoroutineManager.mustBeCompleted
import com.github.onlaait.fbw.utils.JSON
import com.github.onlaait.fbw.utils.UuidAndName
import com.github.onlaait.fbw.utils.cleanJson
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player
import kotlin.io.path.Path
import kotlin.io.path.readText
import kotlin.io.path.writer

object OpSystem {

    private val PATH = Path("ops.json")

    val opPlayers = mutableSetOf<UuidAndName>()

    init {
        load()
        store()
    }

    fun load() {
        Logger.debug { "Loading ops" }
        try {
            opPlayers.addAll(JSON.decodeFromString(PATH.readText()))
        } catch (e: Exception) {
            PATH.writer().use { it.write("[]") }
        }
    }

    fun store() = CoroutineManager.FILE_OUT_SCOPE.launch {
        Logger.debug { "Storing ops" }
        PATH.writer().use { it.write(cleanJson(JSON.encodeToString(opPlayers))) }
    }.mustBeCompleted()

    fun Player.setOp(value: Boolean): Boolean {
        if (value) {
            if (isOp) return false
            opPlayers.add(UuidAndName(uuid, username))
        } else {
            if (!isOp) return false
            val iter = opPlayers.iterator()
            for (e in iter) {
                if (e.uuid != uuid) continue
                iter.remove()
                if (ServerProperties.run { WHITE_LIST && ENFORCE_WHITELIST }) kickIfNotWhitelisted()
                break
            }
        }
        refreshCommands()
        store()
        return true
    }

    val Player.isOp: Boolean
        get() = opPlayers.any { it.uuid == uuid }

    val CommandSender.isOp: Boolean
        get() = this is ConsoleSender || this is Player && isOp
}