package com.github.onlaait.fbw.system

import com.github.onlaait.fbw.serializer.UUIDAsStringSerializer
import com.github.onlaait.fbw.server.Logger
import com.github.onlaait.fbw.server.PlayerP
import com.github.onlaait.fbw.system.Whitelist.kickIfNotWhitelisted
import com.github.onlaait.fbw.utils.CoroutineManager
import com.github.onlaait.fbw.utils.CoroutineManager.mustBeCompleted
import com.github.onlaait.fbw.utils.JsonUtils
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player
import java.util.*
import kotlin.io.path.*

object OpSystem {

    private val PATH = Path("ops.json")
    val opPlayers = mutableSetOf<OpPlayer>()

    init {
        read()
        write()
    }

    fun read() {
        Logger.debug { "Loading ops" }
        if (PATH.isRegularFile()) {
            try {
                opPlayers.addAll(JsonUtils.json.decodeFromString(PATH.readText()))
            } catch (e: IllegalArgumentException) {
                Logger.warn("Something is wrong with the format of '${PATH.name}', initializing it")
            }
        } else {
            PATH.writer().use { it.write("[]") }
        }
    }

    fun write() = CoroutineManager.fileOutputScope.launch {
        Logger.debug { "Storing ops" }
        PATH.writer().use { it.write(JsonUtils.cleanJson(JsonUtils.json.encodeToString(opPlayers))) }
    }.mustBeCompleted()

    @Serializable
    data class OpPlayer(
        @Serializable(with = UUIDAsStringSerializer::class)
        val uuid: UUID,
        val name: String
    )

    val CommandSender.isOp: Boolean
        get() {
            return this is ConsoleSender || (this as PlayerP).isOp
        }

    fun Player.setOp(value: Boolean): Boolean {
        if (value) {
            if (this.isOp) return false
            (this as PlayerP).isOp = true
            opPlayers.add(OpPlayer(this.uuid, this.username))
        } else {
            if (!this.isOp) return false
            (this as PlayerP).isOp = false
            for (e in opPlayers) {
                if (e.uuid == this.uuid) {
                    opPlayers.remove(e)
                    if (ServerProperties.WHITE_LIST && ServerProperties.ENFORCE_WHITELIST) {
                        this.kickIfNotWhitelisted()
                    }
                    break
                }
            }
        }
        this.refreshCommands()
        write()
        return true
    }
}