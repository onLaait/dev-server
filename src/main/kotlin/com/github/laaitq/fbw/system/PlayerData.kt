package com.github.laaitq.fbw.system

import com.charleskorn.kaml.Yaml
import com.github.laaitq.fbw.utils.PlayerUtils
import com.github.laaitq.fbw.utils.PlayerUtils.data
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minestom.server.entity.Player
import java.io.File

object PlayerData {

    private const val dir = "playerdata"

    init {
        File(dir).mkdir()
    }

    private fun getFile(player: Player): File = File(dir + '/' + player.uuid + ".yml")

    fun read(player: Player): PlayerData {
        Logger.debug("Loading player data of ${player.username}")
        val file = getFile(player)
        return if (file.isFile) {
            Yaml.default.decodeFromString(file.readText())
        } else {
            PlayerData(player.username)
        }
    }

    fun write(player: Player) {
        Logger.debug("Storing player data of ${player.username}")
        getFile(player).writeText(Yaml.default.encodeToString(player.data))
    }

    fun writeAllPlayers() = PlayerUtils.allPlayers.forEach { write(it) }

    @Serializable
    data class PlayerData(
        var lastKnownName: String,
        var muteTime: Long? = null,
        var isParticipant: Boolean = true,
    )
}