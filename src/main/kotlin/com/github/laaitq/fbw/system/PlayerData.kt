package com.github.laaitq.fbw.system

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minestom.server.entity.Player
import java.io.BufferedWriter
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.util.*

object PlayerData {
    private const val path = "./playerdata/"
    private val dir = File(path)
    val playersData = hashMapOf<UUID, PlayerData>()

    init {
        dir.mkdir()
        read()
    }

    fun read() {
        val yamls = dir.listFiles()
        for (yaml in yamls!!) if (yaml.isFile) {
            playersData[UUID.fromString(yaml.nameWithoutExtension)] = Yaml.default.decodeFromString(FileReader(yaml).readText())
        }
    }

    fun write(player: Player) {
        BufferedWriter(FileWriter(path + player.uuid + ".yml")).use {
            it.write(Yaml.default.encodeToString(player.playerdata))
        }
    }

    val Player.playerdata: PlayerData
        get() {
            if (!playersData.containsKey(uuid)) {
                playersData[uuid] = PlayerData(username, true)
                write(this)
            }
            return playersData[uuid]!!
        }

    @Serializable
    data class PlayerData(
        var lastKnownName: String,
        var isParticipant: Boolean = true
    )
}