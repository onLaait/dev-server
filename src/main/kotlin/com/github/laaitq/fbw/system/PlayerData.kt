package com.github.laaitq.fbw.system

import com.charleskorn.kaml.Yaml
import com.github.laaitq.fbw.utils.MyCoroutines
import com.github.laaitq.fbw.utils.MyCoroutines.mustBeCompleted
import com.github.laaitq.fbw.utils.PlayerUtils
import com.github.laaitq.fbw.utils.PlayerUtils.data
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import net.minestom.server.entity.Player
import java.nio.file.Path
import kotlin.io.path.*

object PlayerData {

    private const val dirPath = "playerdata"

    init {
        Path(dirPath).createDirectories()
    }

    fun read(player: Player): PlayerData {
        Logger.debug { "Loading player data of ${player.username}" }
        val path = getPath(player)
        return if (path.isRegularFile()) {
            Yaml.default.decodeFromString(path.reader().use { it.readText() })
        } else {
            PlayerData(player.username)
        }
    }

    fun write(player: Player) = MyCoroutines.fileOutputScope.launch {
        Logger.debug { "Storing player data of ${player.username}" }
        getPath(player).writer().use { it.write(Yaml.default.encodeToString(player.data)) }
    }.mustBeCompleted()

    fun writeAllPlayers() = PlayerUtils.allPlayers.forEach { write(it) }

    private fun getPath(player: Player): Path = Path(dirPath + '/' + player.uuid + ".yml")

    @Serializable
    data class PlayerData(
        var lastKnownName: String,
        var muteTime: Long? = null,
        var isParticipant: Boolean = true,
    )
}