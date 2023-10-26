package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.PlayerP
import com.github.laaitq.fbw.serializer.UUIDAsStringSerializer
import com.github.laaitq.fbw.system.Whitelist.kickIfNotWhitelisted
import com.github.laaitq.fbw.utils.JsonUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player
import net.minestom.server.entity.fakeplayer.FakePlayer
import java.io.File
import java.util.*

object OpSystem {

    private const val filePath = "ops.json"
    val opPlayers = mutableSetOf<OpPlayer>()

    init {
        read()
        write()
    }

    fun read() {
        Logger.debug("Loading ops")
        val file = File(filePath)
        if (file.isFile) {
            try {
                opPlayers.addAll(JsonUtils.json.decodeFromString(file.reader().use { it.readText() }))
            } catch (e: IllegalArgumentException) {
                Logger.warn("Something is wrong with the format of '${file.path}', initializing it")
            }
        } else {
            file.writer().use { it.write("[]") }
        }
    }

    fun write() {
        Logger.debug("Storing ops")
        File(filePath).writer().use {
            it.write(JsonUtils.cleanJson(JsonUtils.json.encodeToString(opPlayers)))
        }
    }

    @Serializable
    data class OpPlayer(
        @Serializable(with = UUIDAsStringSerializer::class)
        val uuid: UUID,
        val name: String
    )

    val CommandSender.isOp: Boolean
        get() {
            return this is ConsoleSender || (this !is FakePlayer && (this as PlayerP).isOp)
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