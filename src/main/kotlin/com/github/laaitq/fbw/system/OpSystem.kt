package com.github.laaitq.fbw.system

import com.github.laaitq.fbw.serializer.UUIDAsStringSerializer
import com.github.laaitq.fbw.system.Whitelist.kickIfNotWhitelisted
import com.github.laaitq.fbw.utils.JsonUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player
import net.minestom.server.permission.Permission
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

object OpSystem {
    private const val jsonPath = "ops.json"
    val opPermission = Permission("operator")
    val opPlayersData = mutableSetOf<OpPlayer>()

    init {
        read()
        write()
    }

    fun read() {
        Logger.debug("Loading ops")
        if (File(jsonPath).exists()) {
            try {
                opPlayersData.addAll(JsonUtils.json.decodeFromString(FileReader(jsonPath).use { it.readText() }))
            } catch (e: IllegalArgumentException) {
                Logger.warn("Something is wrong with the format of '${jsonPath}', initializing it")
            }
        } else {
            BufferedWriter(FileWriter(jsonPath)).use {
                it.write("[]")
            }
        }
    }

    fun write() {
        Logger.debug("Storing ops")
        BufferedWriter(FileWriter(jsonPath)).use {
            it.write(JsonUtils.cleanJson(JsonUtils.json.encodeToString(opPlayersData)))
        }
    }

    @Serializable
    data class OpPlayer(
        @Serializable(with = UUIDAsStringSerializer::class)
        val uuid: UUID,
        val name: String
    )

    val Player.isOp: Boolean
        get() {
            return this.hasPermission(opPermission)
        }

    val CommandSender.isOp: Boolean
        get() {
            return this is ConsoleSender || (this as Player).isOp
        }

    fun Player.setOp(value: Boolean): Boolean {
        if (value) {
            if (this.isOp) return false
            this.addPermission(opPermission)
            opPlayersData.add(OpPlayer(this.uuid, this.username))
        } else {
            if (!this.isOp) return false
            this.removePermission(opPermission)
            for (e in opPlayersData) {
                if (e.uuid == this.uuid) {
                    opPlayersData.remove(e)
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