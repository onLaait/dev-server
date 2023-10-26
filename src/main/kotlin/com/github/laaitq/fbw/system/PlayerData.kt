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

    private const val dirPath = "playerdata"

    init {
        File(dirPath).mkdir()
    }

    fun read(player: Player): PlayerData {
        Logger.debug("Loading player data of ${player.username}")
        val file = getFile(player)
        return if (file.isFile) {
            Yaml.default.decodeFromString(file.reader().use { it.readText() })
        } else {
            PlayerData(player.username)
        }
    }

    fun write(player: Player) {
        Logger.debug("Storing player data of ${player.username}")
        getFile(player).writer().use { it.write(Yaml.default.encodeToString(player.data)) }
    }

    fun writeAllPlayers() = PlayerUtils.allPlayers.forEach { write(it) }

    private fun getFile(player: Player): File = File(dirPath + '/' + player.uuid + ".yml")

    @Serializable
    data class PlayerData(
        var lastKnownName: String,
        var muteTime: Long? = null,
        var isParticipant: Boolean = true,
    )
}